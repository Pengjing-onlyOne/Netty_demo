package com.pengjing.client;

import com.alibaba.fastjson2.JSON;
import com.pengjing.message.ChatRequestMessage;
import com.pengjing.message.GroupChatRequestMessage;
import com.pengjing.message.GroupCreateRequestMessage;
import com.pengjing.message.GroupJoinRequestMessage;
import com.pengjing.message.GroupMembersRequestMessage;
import com.pengjing.message.GroupQuitRequestMessage;
import com.pengjing.message.LoginRequestMessage;
import com.pengjing.message.LoginResponseMessage;
import com.pengjing.message.PingMessage;
import com.pengjing.protocol.MessageDecodec4Json;
import com.pengjing.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
//                    socketChannel.pipeline().addLast(LOGGING_HANDLER);
                    socketChannel.pipeline().addLast(MESSAGEDECODECSHARBLE);

                    //添加一个写事件,超过3S没有输入,就自动发一条消息给服务器
                    socketChannel.pipeline().addLast(new IdleStateHandler(0,3,0));
                    //既可以做为入栈处理器,也可以作为出栈处理器
                    socketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            //响应用户的特殊事件
//                            super.userEventTriggered(ctx, evt);
                            //IdleState#READER_IDLE
                            IdleStateEvent event = (IdleStateEvent) evt;
                            if (event.state() == IdleState.WRITER_IDLE) {
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });

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
                            System.out.println("得到的消息是:{}"+JSON.toJSONString(msg));
//                            log.debug("得到的消息是:{}"+JSON.toJSONString(msg));
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
                                                case "gsend":
                                                    ctx.writeAndFlush(new GroupChatRequestMessage(username,messages[1],messages[2]));
                                                    break;
                                                case "gcreat":
                                                    String users = messages[2];
                                                    Set<String> userSet = Arrays.stream(users.split(",")).collect(Collectors.toSet());
                                                    ctx.writeAndFlush(new GroupCreateRequestMessage(username,messages[1],userSet));
                                                    break;
                                                case "gmembers":
                                                    ctx.writeAndFlush(new GroupMembersRequestMessage(messages[1]));
                                                    break;
                                                case "gjoin":
                                                    ctx.writeAndFlush(new GroupJoinRequestMessage(username,messages[1]));
                                                    break;
                                                case "gquit":
                                                    ctx.writeAndFlush(new GroupQuitRequestMessage(username,messages[1]));
                                                    break;
                                                case "quit":
                                                    ctx.channel().close();
                                                    break;
                                                default:
                                                    continue;
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
