package com.netty.update.stickyAndHalfWrapped;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class SickServer {
    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup(2);
        new ServerBootstrap()
                .group(bossGroup,workGroup)
                //设置每次接收的数据的大小,但是在最新的系统里面不管用
//                .option(ChannelOption.SO_RCVBUF,10)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //设置缓冲区的大小
//                        socketChannel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(10));
                         socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(10));
                        /*配置日志*/
                        socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    }
                }).bind(8080);
    }
}
