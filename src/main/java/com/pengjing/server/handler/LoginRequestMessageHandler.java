package com.pengjing.server.handler;

import com.pengjing.message.LoginRequestMessage;
import com.pengjing.message.LoginResponseMessage;
import com.pengjing.service.UserServiceFactory;
import com.pengjing.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Description:
 * @Version: V1.0
 */
//登录处理
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LoginRequestMessage loginRequestMessage) throws Exception {
        String username = loginRequestMessage.getUsername();
        String password = loginRequestMessage.getPassword();
        boolean login = UserServiceFactory.getUserService().login(username, password);
        LoginResponseMessage loginResponseMessage;
        if(login){
            SessionFactory.getSession().bind(channelHandlerContext.channel(),username);
            loginResponseMessage = new LoginResponseMessage(true, "登陆成功");
        }  else {
            loginResponseMessage = new LoginResponseMessage(false, "用户名或密码错误");
        }
        channelHandlerContext.writeAndFlush(loginResponseMessage);
    }
}
