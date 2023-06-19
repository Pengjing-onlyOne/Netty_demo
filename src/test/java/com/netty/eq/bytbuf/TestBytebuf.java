package com.netty.eq.bytbuf;

import com.utils.BytebuUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * @Description:
 * @Version: V1.0
 */
public class TestBytebuf {
    public static void main(String[] args) {
        //支持动态扩容,初始的容量为256
        //扩容的大小为原来的一倍
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append("a");
        }
        buf.writeBytes(sb.toString().getBytes());
        BytebuUtils.log(buf);
    }
}
