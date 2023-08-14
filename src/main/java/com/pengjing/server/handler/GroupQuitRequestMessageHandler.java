package com.pengjing.server.handler;

import com.pengjing.message.GroupQuitRequestMessage;
import com.pengjing.message.GroupQuitResponseMessage;
import com.pengjing.session.GroupSessionFactory;
import com.pengjing.session.SessionFactory;
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
public class GroupQuitRequestMessageHandler extends SimpleChannelInboundHandler<GroupQuitRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GroupQuitRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        String username = msg.getUsername();

        //判断群是否存在
        Boolean group = GroupSessionFactory.getGroupSession().getGroup(groupName);
        Channel channel = SessionFactory.getSession().getChannel(username);
        if(!group){
            channel.writeAndFlush(new GroupQuitResponseMessage(false,"所退出的群不存在"));
        }
        List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
        Set<String> members = GroupSessionFactory.getGroupSession().getMembers(groupName);
        boolean remove = membersChannel.remove(channel);
        boolean remove_user = members.remove(username);
        if(remove && remove_user){
            //向群员发送消息用户退出群聊
            for (Channel channel1 : membersChannel) {
                channel1.writeAndFlush(new GroupQuitResponseMessage(true,"用户"+username+"退出群聊"));
            }
        }else {
            channel.writeAndFlush(new GroupQuitResponseMessage(false,"所退出的群失败"));
            membersChannel.add(channel);
            members.add(username);
        }

    }
}
