package com.pengjing.protocol;

import com.alibaba.fastjson2.JSON;
import com.google.gson.Gson;
import com.pengjing.message.ChatRequestMessage;
import com.pengjing.message.ChatResponseMessage;
import com.pengjing.message.LoginRequestMessage;
import com.pengjing.message.LoginResponseMessage;
import com.pengjing.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * 1.魔数,用于在第一时间判定数据是否有效
 *  2.版本号,可以支持协议的升级
 *  * 3.序列化算法,消息正文所采用的序列化和反序列化的方式0:jdk,1:json,2:protoubuf,3:hessian
 *  * 4.指令类型,是登录,注册,单聊等
 *  * 5.请求序号,为了双工通信,提供异步能力
 *  * 6.正文长度
 *  * 7.消息正文
 */
@Slf4j
@ChannelHandler.Sharable
public class MessageDecodec4Json extends MessageToMessageCodec<ByteBuf, Message> {
    //使用json序列化的方式,编解码数据
    private static final  byte[] magic_num = "P".getBytes();
    @Override
    public  void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        //创建bytebuf
//        ByteBuf buffer = ctx.alloc().buffer();
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(1024);
        //1.魔数
        buffer.writeBytes(magic_num);
        //版本号
        buffer.writeByte(1);
        //序列化算法
        buffer.writeByte(1);
        //指令类型
        buffer.writeInt(msg.getMessageType());
        //请求序号
        buffer.writeInt(msg.getSequenceId());
        byte[] messageBytes = JSON.toJSONBytes(msg);
        //添加无意义字段
        buffer.writeByte(0xff);
        //正文长度
        buffer.writeInt(messageBytes.length);
        //消息正文
        buffer.writeBytes(messageBytes);

        out.add(buffer);

    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {

        //获取魔数
        ByteBuf magicNumBuf = msg.readBytes(magic_num.length);
        //获取版本号
        byte version = msg.readByte();
        //序列化算法
        byte serializableId = msg.readByte();
        //指令类型
        int messageType = msg.readInt();
        //获取请求序号
        int sequenceId = msg.readInt();
        //无意义字段
        msg.readByte();

        //获取正文长度
        int classLen = msg.readInt();
        byte[] classBytes = new byte[classLen];
        msg.readBytes(classBytes,0, classLen);
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        Gson gson = new Gson();
        Message message;
        switch (messageType){
            case 0:
                message = gson.fromJson(new String(classBytes), LoginRequestMessage.class);
                break;
            case 1:
                message = gson.fromJson(new String(classBytes), LoginResponseMessage.class);
                break;
            case 2:
                message = gson.fromJson(new String(classBytes), ChatRequestMessage.class);
                break;
            case 3:
                message = gson.fromJson(new String(classBytes), ChatResponseMessage.class);
                break;
            default:
                message = null;
        }

//        Message message = JSONObject.parseObject(new String(classBytes), Message.class);
        System.out.println("序列化的数据为:"+gson.toJson(message));
        out.add(message);

        log.debug("魔数:{},版本号:{},序列化算法:{},指令类型:{},获取请求序列号:{},内容长度:{}", StandardCharsets.UTF_8.decode(magicNumBuf.nioBuffer()),version,serializableId,messageType,sequenceId,classLen);
        log.debug("对象为:{}",message);
    }
}
