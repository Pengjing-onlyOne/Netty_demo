package com.pengjing.protocol;

import com.pengjing.config.Config;
import com.pengjing.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * 1.魔数,用于在第一时间判定数据是否有效
 * 2.版本号,可以支持协议的升级
 * 3.序列化算法,消息正文所采用的序列化和反序列化的方式0:jdk,1:json,2:protoubuf,3:hessian
 * 4.指令类型,是登录,注册,单聊等
 * 5.请求序号,为了双工通信,提供异步能力
 * 6.正文长度
 * 7.消息正文
 */
@Slf4j
public class MessageDecodec extends ByteToMessageCodec<Message> {
    //设置魔数
    private static  final  byte[] magic_num = "peng".getBytes();
    //自定义的编码操作,需要的相关数据为,固定的字节数最好是2的整数倍
    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        //1.魔数,java使用的是cafeebabe
        out.writeBytes(magic_num);
        //2.版本号
        out.writeByte(1);
        //3.序列化算法
        out.writeByte(Config.getSerialDecodec().ordinal());
        //4.指令类型
        out.writeInt(msg.getMessageType());
        //5.请求序号
        out.writeInt(msg.getSequenceId());
        //无意义,对齐使用
        out.writeByte(0xff);
        out.writeByte(0xff);
        //获取对象字节
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        //对象
        byte[] bytes = bos.toByteArray();
        //6.正文长度
        out.writeInt(bytes.length);

        //消息正文
        out.writeBytes(bytes);



    }
    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Message message = null;
        byte[] bytes_magic = new byte[magic_num.length];
        //1.魔数
        in.readBytes(bytes_magic, 0, magic_num.length);

        ByteBuf buffer =  ByteBufAllocator.DEFAULT.buffer(bytes_magic.length);
        buffer.writeBytes(bytes_magic);
        //版本号
        byte version = in.readByte();
        //序列化算法
        byte serializerType = in.readByte();
        //指令
        int  messageType= in.readInt();
        //请求序号
        int sequenceId = in.readInt();
        //无意义数据
        in.readByte();
        in.readByte();
        //对象长度
        int lenth = in.readInt();
        byte[] bytes = new byte[lenth];
        in.readBytes(bytes,0,lenth);
        //反序列化为对象
        Class<? extends Message> messageClass = Message.getMessageClass(Serial.decodec.values()[serializerType].ordinal());
        message = Serial.decodec.values()[serializerType].encode(messageClass,bytes);
        log.debug("魔数是:{},版本号:{},序列化算法:{},:请求序号:{},{},对象长度:{}", StandardCharsets.UTF_8.decode(buffer.nioBuffer()),version,serializerType,messageType,sequenceId,lenth);
        log.debug("{}",message);
        out.add(message);
    }
}
