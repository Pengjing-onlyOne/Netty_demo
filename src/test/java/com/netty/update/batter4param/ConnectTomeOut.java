package com.netty.update.batter4param;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Description:
 * @Version: V1.0
 */
//连接超时参数的设置和相关源码
@Slf4j
public class ConnectTomeOut {
    public static void main(String[] args) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(new NioEventLoopGroup());
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000);
            bootstrap.handler(new LoggingHandler());
            ChannelFuture future = bootstrap.connect(new InetSocketAddress("124.221.132.142", 8081));
            future.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.debug(e.getMessage());
        }
    }
}
