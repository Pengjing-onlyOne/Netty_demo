package com.pengjing.server.handler;

import com.pengjing.message.GroupChatResponseMessage;
import com.pengjing.message.GroupJoinRequestMessage;
import com.pengjing.session.Group;
import com.pengjing.session.GroupSessionFactory;
import com.pengjing.session.SessionFactory;
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
public class GroupJoinRequestMessageHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupJoinRequestMessage msg) throws Exception {
        String username = msg.getUsername();
        String groupName = msg.getGroupName();
        //校验群是否存在
        Boolean group1 = GroupSessionFactory.getGroupSession().getGroup(groupName);
        Channel channel = SessionFactory.getSession().getChannel(username);
        if(!group1){

            channel.writeAndFlush(new GroupChatResponseMessage(false,"所添加的群不存在"));
        }
        Group group = GroupSessionFactory.getGroupSession().joinMember(groupName,username);
        if(group != null){
            channel.writeAndFlush(new GroupChatResponseMessage(true,"加群成功"));
            //向群员发送组员进入消息
            List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
            for (Channel channel1 : membersChannel) {
                channel1.writeAndFlush(new GroupChatResponseMessage(true,"用户"+username+"加入群聊"));
            }
        }else {
            channel.writeAndFlush(new GroupChatResponseMessage(false,"加群失败"));
        }
    }
}
