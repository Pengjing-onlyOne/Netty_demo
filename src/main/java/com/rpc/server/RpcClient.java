package com.rpc.server;

import com.pengjing.message.RpcRequestMessage;
import com.pengjing.protocol.MessageDecodecSharble;
import com.pengjing.protocol.ProcotolFrameDecoder;
import com.pengjing.server.handler.RpcResponseMessageHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        //日志打印
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        //消息的编解码
        MessageDecodecSharble MESSAGE_HANDLER = new MessageDecodecSharble();
        //rpc请求处理
        RpcResponseMessageHandler RPC_RESPONSE = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventExecutors);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //解决消息黏包半包
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    //打印日志
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    //消息编解码
                    ch.pipeline().addLast(MESSAGE_HANDLER);
                    //处理rpc消息
                    ch.pipeline().addLast(RPC_RESPONSE);
                }
            });
            Channel channel = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
            ChannelFuture channelFuture = channel.writeAndFlush(new RpcRequestMessage(1, "com.pengjing.service.HelloService", "sayHello", String.class, new Class[]{String.class}, new Object[]{"张三"}));
            channelFuture.addListener(promise->{
                if (!promise.isSuccess()) {
                    Throwable cause = promise.cause();
                    log.error("{}",cause);
                    throw new RuntimeException(cause);
                }
            });
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("client error",e.getMessage());
        }finally {
            eventExecutors.shutdownGracefully();
        }
    }
}
