package com.netty.update.stickyAndHalfWrapped.delimiter;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * 使用系统自带的换行符进行消息的分割
 */
@Slf4j
public class DelimiterClient {
    public static void main(String[] args) throws InterruptedException {
        new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //打印日志
                        socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                Random r =new Random();
                                ByteBuf buf = ctx.alloc().buffer();
                                String message = "";
                                for (int i = 0; i < 10; i++) {
                                     message +=  createMessage(r.nextInt(256) + 1, i + "");
//                                     buf.writeBytes(message.getBytes());
                                }
                                //如果字符组不是Bytebuf会导致发送不出去
                                buf.writeBytes(message.getBytes());
                                ctx.writeAndFlush(buf);
//                                super.channelActive(ctx);
                            }
                        });
                    }
                }).connect(new InetSocketAddress("127.0.0.1",8080)).sync().channel().read();
    }

    //创建消息发
    public static String createMessage(int leng,String s){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leng; i++) {
            sb.append(s);
        }
        sb.append("\n");
        return sb.toString();
    }
}
