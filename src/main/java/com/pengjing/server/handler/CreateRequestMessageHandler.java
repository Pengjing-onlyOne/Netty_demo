package com.pengjing.server.handler;

import com.pengjing.message.GroupCreateRequestMessage;
import com.pengjing.message.GroupCreateResponseMessage;
import com.pengjing.session.Group;
import com.pengjing.session.GroupSession;
import com.pengjing.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

/**
 * @Description:
 * @Version: V1.0
 */
@ChannelHandler.Sharable
public class CreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupCreateRequestMessage groupCreateRequestMessage) throws Exception {
        String groupName = groupCreateRequestMessage.getGroupName();
        Set<String> members = groupCreateRequestMessage.getMembers();
        String groupBoss = groupCreateRequestMessage.getGroupBoss();
        members.add(groupBoss);
        //群管理器
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.createGroup(groupName, members);
        if(group == null){
            //发送成功消息
            channelHandlerContext.writeAndFlush(new GroupCreateResponseMessage(true,groupName+"创建成功"));
            //发送拉群消息
            List<Channel> membersChannel = groupSession.getMembersChannel(groupName);
            for (Channel channel : membersChannel) {
                channel.writeAndFlush(new GroupCreateResponseMessage(true,"您已被拉入"+groupName));
            }
        }else {
            channelHandlerContext.writeAndFlush(new GroupCreateResponseMessage(false,groupName+"已经存在"));
        }
    }
}
