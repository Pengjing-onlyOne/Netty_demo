package com.pengjing.server.handler;

import com.pengjing.message.GroupChatRequestMessage;
import com.pengjing.message.GroupChatResponseMessage;
import com.pengjing.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

/**
 * @Description:
 * @Version: V1.0
 */
@ChannelHandler.Sharable
public class GroupChatRequestMessageHandler extends SimpleChannelInboundHandler<GroupChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupChatRequestMessage groupChatRequestMessage) throws Exception {
        //获取所在的用户
        String groupName = groupChatRequestMessage.getGroupName();
        String content = groupChatRequestMessage.getContent();
        String from = groupChatRequestMessage.getFrom();
        //校验所在聊天组是否存在
        Boolean group = GroupSessionFactory.getGroupSession().getGroup(groupName);
        if(!group){
            channelHandlerContext.writeAndFlush(new GroupChatResponseMessage(false,"发送消息的组不存在"));
        }
        //根据组名获取所存在的用户
        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
        for (Channel channel : membersChannel) {
            channel.writeAndFlush(new GroupChatResponseMessage(from,content));
        }
    }
}
