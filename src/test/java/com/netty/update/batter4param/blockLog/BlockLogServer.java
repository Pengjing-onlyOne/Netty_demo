package com.netty.update.batter4param.blockLog;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class BlockLogServer {
    public static void main(String[] args) {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(new NioEventLoopGroup());
            serverBootstrap.option(ChannelOption.SO_BACKLOG,3);//设置连接为两个
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new LoggingHandler());
            ChannelFuture channelFuture = serverBootstrap.bind(8080);
            channelFuture.sync().channel().read();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
