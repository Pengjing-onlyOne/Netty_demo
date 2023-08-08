package com.pengjing.server;

import com.pengjing.message.LoginRequestMessage;
import com.pengjing.message.LoginResponseMessage;
import com.pengjing.protocol.MessageDecodec4Json;
import com.pengjing.protocol.ProcotolFrameDecoder;
import com.pengjing.service.UserServiceFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Description:
 * @Version: V1.0
 */
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup bossEvent = new NioEventLoopGroup();
        NioEventLoopGroup workerEvent = new NioEventLoopGroup(2);
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageDecodec4Json MESSAGEDECODECSHARBLE = new MessageDecodec4Json();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossEvent, workerEvent);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel)  {
                    //填写相关逻辑,帧解码器
                    socketChannel.pipeline().addLast(new ProcotolFrameDecoder());
                    socketChannel.pipeline().addLast(LOGGING_HANDLER);
                    socketChannel.pipeline().addLast(new ProcotolFrameDecoder());
                    socketChannel.pipeline().addLast(MESSAGEDECODECSHARBLE);
                    socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<LoginRequestMessage>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, LoginRequestMessage loginRequestMessage) throws Exception {
                            String username = loginRequestMessage.getUsername();
                            String password = loginRequestMessage.getPassword();
                            boolean login = UserServiceFactory.getUserService().login(username, password);
                             LoginResponseMessage loginResponseMessage;
                            loginResponseMessage = login  ? new LoginResponseMessage(true,"登陆成功") :  new LoginResponseMessage(false,"用户名或密码错误");
                            channelHandlerContext.writeAndFlush(loginResponseMessage);
                            System.out.println("发送消息了.....");
                        }
                    });
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
