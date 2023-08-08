package com.pengjing.client;

import com.alibaba.fastjson2.JSON;
import com.pengjing.message.LoginRequestMessage;
import com.pengjing.protocol.MessageDecodec4Json;
import com.pengjing.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @Description:
 * @Version: V1.0
 */
//聊天室,客户端代码
@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup workerEvent = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageDecodec4Json MESSAGEDECODECSHARBLE = new MessageDecodec4Json();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerEvent);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel)  {
                    //填写相关逻辑,帧解码器
                    socketChannel.pipeline().addLast(new ProcotolFrameDecoder());
                    socketChannel.pipeline().addLast(LOGGING_HANDLER);
                    socketChannel.pipeline().addLast(MESSAGEDECODECSHARBLE);

                    //发送消息
                    socketChannel.pipeline().addLast("Login_request",new ChannelInboundHandlerAdapter(){

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("得到的消息是:{}"+JSON.toJSONString(msg));
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            //在连接建立的时候,发送消息,登录服务器
                            ThreadFactory threadFactory = Executors.defaultThreadFactory();
                            threadFactory.newThread(()->{
                                Scanner scanner = new Scanner(System.in);
                                System.out.println("请输入用户名");
                                String username = scanner.nextLine();
                                System.out.println("请输入密码");
                                String password = scanner.nextLine();
                                LoginRequestMessage loginRequestMessage = new LoginRequestMessage(username, password);
                                ctx.writeAndFlush(loginRequestMessage);
                            }).start();
                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("127.0.0.1", 8080).sync().channel();
            channel.closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            workerEvent.shutdownGracefully();
        }
    }
}
