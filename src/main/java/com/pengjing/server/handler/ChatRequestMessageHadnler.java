package com.pengjing.server.handler;

import com.pengjing.message.ChatRequestMessage;
import com.pengjing.message.ChatResponseMessage;
import com.pengjing.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Description:
 * @Version: V1.0
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHadnler extends SimpleChannelInboundHandler<ChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ChatRequestMessage chatRequestMessage) throws Exception {
        //获取接收方
        String to_name = chatRequestMessage.getTo();
        Channel channel = SessionFactory.getSession().getChannel(to_name);
        if(channel != null){
            channel.writeAndFlush(new ChatResponseMessage(chatRequestMessage.getFrom(), chatRequestMessage.getContent()));
        }else {
            channelHandlerContext.writeAndFlush(new ChatResponseMessage(to_name,"当前用户不在线"));
        }
    }
}
