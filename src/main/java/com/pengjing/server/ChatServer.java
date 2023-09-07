package com.pengjing.server;

import com.pengjing.protocol.MessageDecodecSharble;
import com.pengjing.protocol.ProcotolFrameDecoder;
import com.pengjing.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @Description:
 * @Version: V1.0
 */
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup bossEvent = new NioEventLoopGroup();
        NioEventLoopGroup workerEvent = new NioEventLoopGroup(2);
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        //消息解码
//        MessageDecodec4Json MESSAGEDECODECSHARBLE = new MessageDecodec4Json();
        MessageDecodecSharble MESSAGEDECODECSHARBLE = new MessageDecodecSharble();
        //登录请求处理器
        LoginRequestMessageHandler LOGIN_HANDLER = new LoginRequestMessageHandler();
        //聊天请求处理器
        ChatRequestMessageHadnler CHAT_HANDLER = new ChatRequestMessageHadnler();
        //群聊创建处理器
        CreateRequestMessageHandler GROUP_CREATE_HANDLER = new CreateRequestMessageHandler();
        //群聊处理器
        GroupChatRequestMessageHandler G_CHAT_HANDLER = new GroupChatRequestMessageHandler();
        //加入群聊处理器
        GroupJoinRequestMessageHandler G_JOIN_HANDLER = new GroupJoinRequestMessageHandler();
        //请求群聊人数处理器
        GroupMembersRequestMessageHandler G_MEMBER_HANDLER = new GroupMembersRequestMessageHandler();
        //群聊退出处理器
        GroupQuitRequestMessageHandler G_QUIT_HANDLER = new GroupQuitRequestMessageHandler();
        //聊天退出处理器
        QuitHandler QUIT_HANDLER = new QuitHandler();


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossEvent, workerEvent);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel)  {

                    socketChannel.pipeline().addLast(new ProcotolFrameDecoder());
                    socketChannel.pipeline().addLast(MESSAGEDECODECSHARBLE);

                    //填写相关逻辑,帧解码器
//                    socketChannel.pipeline().addLast(new ProcotolFrameDecoder());
//                    socketChannel.pipeline().addLast(LOGGING_HANDLER);
                    //表示超过5S没有读到消息就会触发一个连接超时的事件
                    socketChannel.pipeline().addLast(new IdleStateHandler(5,0,0));
                    //既可以做为入栈处理器,也可以作为出栈处理器
                    socketChannel.pipeline().addLast(new ChannelDuplexHandler(){
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            //响应用户的特殊事件
//                            super.userEventTriggered(ctx, evt);
                            //IdleState#READER_IDLE
                            IdleStateEvent event = (IdleStateEvent) evt;
                            if (event.state() == IdleState.READER_IDLE) {
                                System.out.println("超过了5S没有读到数据");
                                ctx.close();
                            }
                        }
                    });
                    socketChannel.pipeline().addLast(LOGIN_HANDLER);
                    socketChannel.pipeline().addLast(CHAT_HANDLER);
                    socketChannel.pipeline().addLast(GROUP_CREATE_HANDLER);
                    socketChannel.pipeline().addLast(G_CHAT_HANDLER);
                    socketChannel.pipeline().addLast(G_JOIN_HANDLER);
                    socketChannel.pipeline().addLast(G_MEMBER_HANDLER);
                    socketChannel.pipeline().addLast(G_QUIT_HANDLER);
                    socketChannel.pipeline().addLast(QUIT_HANDLER);
                }
            });
            Channel channel = serverBootstrap.bind(8080).sync().channel();
            channel.closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossEvent.shutdownGracefully();
            workerEvent.shutdownGracefully();
        }
    }

}
