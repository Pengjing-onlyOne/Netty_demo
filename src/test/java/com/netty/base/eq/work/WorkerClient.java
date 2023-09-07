package com.netty.base.eq.work;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

import static com.utils.BytebuUtils.log;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class WorkerClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Channel channel = new Bootstrap().group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                nioSocketChannel.pipeline().addLast(new StringEncoder());
                nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                    super.channelRead(ctx, msg);
                        ByteBuf byteBuf = (ByteBuf) msg;
                        log(byteBuf);
                        WorkerClient.log.debug("客户端读取的消息是:"+byteBuf.toString());
                        super.channelRead(ctx, msg);
                    }
                });
            }
        }).connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
        new Thread(()->{
            Scanner scanner = new Scanner(System.in);
            while(true) {
                String s = scanner.nextLine();
                if ("q".equals(s)) {
                    channel.writeAndFlush(s);
//                    优雅关闭客户端
                    channel.close();
                    break;
                }else {
                    channel.writeAndFlush(s);
                }
            }
        },"write_1").start();

        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                group.shutdownGracefully();
            }
        });

    }

}
