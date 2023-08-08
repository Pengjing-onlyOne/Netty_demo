package com.pengjing.protocol;

import com.pengjing.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
@ChannelHandler.Sharable
/**
 * 必须和帧解码器一起使用:LengthFieldBasedFrameDecoder ,确保接收的bytebuf的消息是完整的
 */
public class MessageDecodecSharble extends MessageToMessageCodec<ByteBuf, Message> {

    //设置魔数
    private static  final  byte[] magic_num = "P".getBytes();

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        //1.魔数,java使用的是cafeebabe
        out.writeBytes(magic_num);
        //2.版本号
        out.writeByte(1);
        //3.序列化算法
        out.writeByte(0);
        //4.指令类型
        out.writeInt(msg.getMessageType());
        //5.请求序号
        out.writeInt(msg.getSequenceId());
        //无意义,对齐使用
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

        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

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
        //对象长度
        int lenth = in.readInt();
        byte[] bytes = new byte[lenth];
        in.readBytes(bytes,0,lenth);
        //反序列化为对象
        if(serializerType == 0){
            //使用jdk转对象
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            message =(Message) ois.readObject();
        }
        log.debug("魔数是:{},版本号:{},序列化算法:{},:请求序号:{},{},对象长度:{}", StandardCharsets.UTF_8.decode(buffer.nioBuffer()),version,serializerType,messageType,sequenceId,lenth);
        log.debug("{}",message);
        out.add(message);
    }
}
