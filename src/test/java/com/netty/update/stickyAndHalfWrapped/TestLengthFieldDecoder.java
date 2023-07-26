package com.netty.update.stickyAndHalfWrapped;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Description:
 * @Version: V1.0
 */
public class TestLengthFieldDecoder {
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                /**
                 * 接收的最大数
                 * 记录长度的偏移量,偏移几个字节是长度域
                 * 长度所占用的字节数
                 * 长度的调整
                 * 需要跳过的字节数
                 */
                new LoggingHandler(LogLevel.DEBUG),
                new LengthFieldBasedFrameDecoder(1024,8,4,0,12),
                new LoggingHandler(LogLevel.DEBUG)
        );

        //4个字节的内容长度 实际内容
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        send(buf,"Hello, World");
        send(buf,"Hi");
        channel.writeInbound(buf);
    }

    public static void send(ByteBuf buf,String content){
        byte[] bytes = content.getBytes();
        //如果在消息的长度前面添加了相对应的长度或者数据,就需要使用,lengthFieldOffset调整接收的消息的长度
        buf.writeBytes("pengjing".getBytes());
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }
}
