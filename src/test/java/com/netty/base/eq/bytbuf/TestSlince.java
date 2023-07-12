package com.netty.base.eq.bytbuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import static com.utils.BytebuUtils.log;

/**
 * @Description:
 * @Version: V1.0
 */
public class TestSlince {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        for (int i = 97; i < 107; i++) {
            char c = (char) i;
            buf.writeBytes((c+"").getBytes());
        }
//        buf.clear();
        log(buf);

        //在切片的过程中不会使用新的内存
        ByteBuf f1 = buf.slice(0, 5);
        ByteBuf f2 = buf.slice(5, 5);

        log(f1);
        log(f2);

        //成本减少,维护难度增加
        CompositeByteBuf bufs = ByteBufAllocator.DEFAULT.compositeBuffer();
        bufs.addComponents(true,f1,f2);
        log(bufs);

        ByteBuf byteBuf = Unpooled.wrappedBuffer(f1, f2);
        log(byteBuf);
    }
}
