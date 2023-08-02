package com.encodec;

import com.encodec.message.LoginRequestMessage;
import com.encodec.protocol.MessageDecodec;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @Description:
 * @Version: V1.0
 */
public class EmbeddedTestMessage {
    public static void main(String[] args) {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new LoggingHandler(),
                new MessageDecodec()
        );
        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan","123");
        embeddedChannel.writeOutbound(loginRequestMessage);
    }
}
