package com.netty.base.eq.bytbuf;

import com.utils.BytebuUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * 如果自定义的容量比较小,那么在扩容的时候也是会按照2的n次方来扩容
 */
public class TestBytebuf {
    public static void main(String[] args) {
        //支持动态扩容,初始的容量为256
        //扩容的大小为原来的一倍
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 33; i++) {
            sb.append("a");
        }
        char c = 'a';
        buf.writeBytes(sb.toString().getBytes());
        BytebuUtils.log(buf);
    }
}
