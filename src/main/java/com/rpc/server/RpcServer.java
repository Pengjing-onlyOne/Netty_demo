package com.rpc.server;

import com.pengjing.protocol.MessageDecodecSharble;
import com.pengjing.protocol.ProcotolFrameDecoder;
import com.pengjing.server.handler.RpcRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class RpcServer {
    public static void main(String[] args) {
        //创建两个工作对象
        NioEventLoopGroup bossEvent = new NioEventLoopGroup();
        NioEventLoopGroup workerEvent = new NioEventLoopGroup();
        //创建日志
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        //创建消息处理
        MessageDecodecSharble MESSAGE_CODEC = new MessageDecodecSharble();
        //rpc消息处理
        RpcRequestMessageHandler RPC_HANDLER = new RpcRequestMessageHandler();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(bossEvent,workerEvent);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //黏包半包处理
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    //打印日志
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    //消息的编解码
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    //使用rpc请求
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });

            Channel channel = serverBootstrap.bind(8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error",e.getMessage());
//            e.printStackTrace();
        }finally {
            bossEvent.shutdownGracefully();
            workerEvent.shutdownGracefully();
        }
    }
}
