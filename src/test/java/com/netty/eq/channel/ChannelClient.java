package com.netty.eq.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ChannelClient {
    public static void main(String[] args) throws InterruptedException {
        //2 带有Future Promise的类型都是和异步方法配套使用,用来处理结果
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                //1.连接到服务器
                //异步非阻塞,main发起了作用,真正执行connect是nio线程
                .connect(new InetSocketAddress("127.0.0.1", 8080));

        //2.1使用sync()方法,同步处理结果
        //阻塞当前线程,知道nio线程连接建立完毕
        /*channelFuture.sync();
        Channel channel = channelFuture.channel();
        log.debug("{}",channel);
        channel.writeAndFlush("1111");*/

        //2.2使用addListener方法,异步处理结果
        channelFuture.addListener(new ChannelFutureListener() {
            //nio在线程建立好之后,会调用operationComplate
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel channel = channelFuture.channel();
                log.debug("{}",channel);
                channel.writeAndFlush("我是方法2");
            }
        });
    }
}
