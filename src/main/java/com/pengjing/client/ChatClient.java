package com.pengjing.client;

import com.alibaba.fastjson2.JSON;
import com.pengjing.message.ChatRequestMessage;
import com.pengjing.message.LoginRequestMessage;
import com.pengjing.message.LoginResponseMessage;
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
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

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
        //线程之间通信
        CountDownLatch WAIT_FORLOGIN = new CountDownLatch(1);
        AtomicBoolean LOGIN = new AtomicBoolean(false);
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
                            if(msg instanceof LoginResponseMessage){
                                LoginResponseMessage response = (LoginResponseMessage) msg;
                                if(response.isStatus()){
                                    LOGIN.set(true);
                                }
                                WAIT_FORLOGIN.countDown();
                            }
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

                                //等待线程结束
                                try {
                                    WAIT_FORLOGIN.await();
                                    if(!LOGIN.get()){
                                        ctx.channel().close();
                                    }
                                    //如果登录失败,重新输入用户名和密码登录
                                    while(true){
                                        System.out.println("============================================");
                                        System.out.println("send [username] [content]");
                                        System.out.println("gsend [group name] [content]");
                                        System.out.println("gcreat [group name] [m1,m2,m3....]");
                                        System.out.println("gmembers [group name] ");
                                        System.out.println("gjoin [group name] ");
                                        System.out.println("gquit [group name] ");
                                        System.out.println("quit ");
                                        System.out.println("============================================");
                                        String message = scanner.nextLine();
                                        if(!StringUtil.isNullOrEmpty(message)) {
                                            String[] messages = message.split(" ");
                                            switch(messages[0]){
                                                case "send":
                                                    ctx.writeAndFlush(new ChatRequestMessage(username,messages[1],messages[2]));
                                                    break;
                                               /* case "gsend":
                                                    break;
                                                case "gcreat":
                                                    break;
                                                case "gmembers":
                                                    break;
                                                case "gjoin":
                                                    break;
                                                case "gquit":
                                                    break;
                                                case "quit":
                                                    break;*/
                                            }

                                        }else {
                                            System.out.println("输入为空,请重新输入");
                                            continue;
                                        }

                                    }
                                    //如果没有失败,进入连天界面
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
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
