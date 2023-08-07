package com.test;

import com.encodec.message.LoginRequestMessage;
import com.encodec.protocol.MessageDecodec4Json;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class MessageDecodecTest {
    @Test
    public void test() throws Exception {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new LoggingHandler(LogLevel.DEBUG),
                ////防止半包问题,添加帧解码器
                new LengthFieldBasedFrameDecoder(1024,16,4,0,0),
                new MessageDecodec4Json()
        );
        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan","123");
        embeddedChannel.writeOutbound(loginRequestMessage);

        //添加一个入栈操作
//        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(1024);
        List<Object> out = new ArrayList<>();
        new MessageDecodec4Json().encode(null,loginRequestMessage,out);
        ByteBuf buffer = (ByteBuf) out.get(0);
//        log.debug("对象数组的长度为:{}",out.size());
        embeddedChannel.writeInbound(buffer);
    }
}
