package com.netty.eq.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * q_1:如果需要再channel退出后做一些收尾工作,应该怎么操作
 * 项目中有q_1.1的注释,表示的是将后续收尾的工作都放在这里并不合适,通过日志可以知道他们不是在一个线程中的,所以无法保证他们执行的顺序
 * 应该使用的是channel自带的方法来进行后续收尾
 * q_1.2 channel.closeFuture().sync();是一个同步的阻塞方法,会将main线程阻塞住,在执行完channel的关闭动作之后,开始执行后面的代码,可以实现后续收尾工作
 * q_1.3 channel.closeFuture().addListener使用的是异步的操作,收尾的工作是在nio的线程中完成,可以保证他的执行顺序
 * q_2:为什么退出后,项目并没有退出,依旧在运行?应该怎么操作让它随用户的退出一起关闭?
 * 需要关闭的还有NioEventLoopGroup,将这个资源关闭之后,系统就会停止运行
 *  使用group.shutdownGracefully();方法,表示优雅关闭,注释为在关闭期间不接受任务,如果接受了任务就会将任务执行完,并重新计算是时间,2s内接受到任务就重新计时
 *  15s为超时时间
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class ChannelFutureClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap().group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                //netty自带的日志记录器
                nioSocketChannel.pipeline().addLast(new LoggingHandler());
                nioSocketChannel.pipeline().addLast(new StringEncoder());
            }
        }).connect(new InetSocketAddress("127.0.0.1", 8080)).channel();

        //创建一个新的线程用于发送消息
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String msg = scanner.nextLine();
                if ("q".equals(msg)) {
                    //在用户输入q之后,退出聊天
                    channel.close();
//  q_1.1              log.debug("后续收尾的工作......");
                    break;
                }
                channel.writeAndFlush(msg);
            }
        }, "input").start();
// q_1.1       log.debug("后续收尾的工作......");

        //q_1.2 channel.closeFuture().sync();是一个同步的阻塞方法,会将main线程阻塞住,在执行完channel的关闭动作之后,开始执行后面的代码,可以实现后续收尾工作
       /* ChannelFuture future = channel.closeFuture().sync();
        log.debug("后续收尾的工作......");*/

        //q_1.3 channel.closeFuture().addListener使用的是异步的操作,收尾的工作是在nio的线程中完成,可以保证他的执行顺序
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("后续收尾的工作......");
                group.shutdownGracefully();
                channel.writeAndFlush("bbbbb");
            }
        });

    }
}
