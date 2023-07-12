package com.utils;

import io.netty.buffer.ByteBuf;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static sun.security.pkcs11.wrapper.Constants.NEWLINE;

/**
 * @Description:
 * @Version: V1.0
 */
public class BytebuUtils {

    public static void log(ByteBuf byteBuf){
        int length = byteBuf.readableBytes();
        int row = length / 16+(length % 15 == 0 ? 0:1)+ 4;
        StringBuilder buf = new StringBuilder(row * 80 * 2)
                .append("read index: ").append(byteBuf.readerIndex())
                .append("write index: ").append(byteBuf.writerIndex())
                .append("capacity:").append(byteBuf.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(buf,byteBuf);
        System.out.println(buf.toString());

    }
}
