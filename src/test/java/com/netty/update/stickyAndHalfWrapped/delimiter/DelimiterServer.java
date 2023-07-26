package com.netty.update.stickyAndHalfWrapped.delimiter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * 使用换行符来界定消息
 */
@Slf4j
public class DelimiterServer {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup bossEvent = new NioEventLoopGroup();
        NioEventLoopGroup workEvent = new NioEventLoopGroup(2);
        new ServerBootstrap()
                .group(bossEvent,workEvent)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //将消息解码
                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                        //自定义结尾符
//                        socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024));
                        socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    }
                }).bind(8080);
    }
}
