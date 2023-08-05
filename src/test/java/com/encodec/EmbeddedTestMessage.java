package com.encodec;

import com.encodec.message.LoginRequestMessage;
import com.encodec.protocol.MessageDecodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Description:
 * @Version: V1.0
 */
public class EmbeddedTestMessage {
    public static void main(String[] args) throws Exception {

        //LengthFieldBasedFrameDecoder handle是不能共用的,在多线程下可能会将不同的数据拼接在一起
        //能否支持多线程使用的handle.添加有@Sharable表示可以公用
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new LoggingHandler(),
                //防止半包问题,添加帧解码器
                new LengthFieldBasedFrameDecoder(1024,16,4,0,0),
                new MessageDecodec()
        );

        //encode
        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan","123");
        embeddedChannel.writeOutbound(loginRequestMessage);

        //decode 解码器
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(1024);
        new MessageDecodec().encode(null,loginRequestMessage,buffer);

        //设置切片
        ByteBuf slice = buffer.slice(0, 100);
        ByteBuf slice_1 = buffer.slice(100, buffer.readableBytes()-100);
        //入栈操作会激活解码器
        embeddedChannel.writeInbound(slice); //会调用release方法,将buff应用计数-1
        buffer.retain(); //将应用计数+1
        embeddedChannel.writeInbound(slice_1);

    }
}
