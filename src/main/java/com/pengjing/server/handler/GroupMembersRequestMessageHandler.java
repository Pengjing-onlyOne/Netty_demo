package com.pengjing.server.handler;

import com.pengjing.message.GroupMembersRequestMessage;
import com.pengjing.message.GroupMembersResponseMessage;
import com.pengjing.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Set;

/**
 * @Description:
 * @Version: V1.0
 */
@ChannelHandler.Sharable
public class GroupMembersRequestMessageHandler extends SimpleChannelInboundHandler<GroupMembersRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupMembersRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        //获取群聊用户名
        Set<String> members = GroupSessionFactory.getGroupSession().getMembers(groupName);
        channelHandlerContext.writeAndFlush(new GroupMembersResponseMessage(members));
    }
}
