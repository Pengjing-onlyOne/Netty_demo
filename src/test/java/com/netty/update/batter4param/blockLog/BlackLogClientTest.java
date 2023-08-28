package com.netty.update.batter4param.blockLog;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class BlackLogClientTest {
    public static void main(String[] args) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(new NioEventLoopGroup());
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new LoggingHandler());
            ChannelFuture future = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080));
            future.sync().channel().read();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.debug(e.getMessage());
        }
    }
}
