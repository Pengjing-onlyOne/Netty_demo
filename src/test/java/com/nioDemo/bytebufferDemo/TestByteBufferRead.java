package com.nioDemo.bytebufferDemo;

import java.nio.ByteBuffer;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugAll;

/**
 * @Description:
 * @Version: V1.0
 */
public class TestByteBufferRead {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a','b','c','d'});

        //设置为读模式
        buffer.flip();
        byte b = buffer.get(0);
        System.out.println((char) b);
        System.out.println((char)  buffer.get(1));
        debugAll(buffer);
        //使用rewind重复度
        System.out.println((char)  buffer.get());
        System.out.println((char)  buffer.get());
        buffer.rewind();
        debugAll(buffer);

        //mark & reset
        //mark 做一个标记,记录position的位置,reset是将position重置到mark的位置
        System.out.println((char)  buffer.get());
        System.out.println((char)  buffer.get());
        buffer.mark();//加标记,索引2的位置
        System.out.println((char)  buffer.get());
        System.out.println((char)  buffer.get());
        buffer.reset();
        System.out.println((char)  buffer.get());
        System.out.println((char)  buffer.get());

    }
}
