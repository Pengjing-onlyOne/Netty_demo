package com.nioDemo.bytebufferDemo;

import java.nio.ByteBuffer;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugAll;

/**
 * @Description:
 * @Version: V1.0
 */
public class TestByteBufferReadOrWrite {
    public static void main(String[] args) {
        //获取一个ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(10);

        //往buffer里面写入数据
        buffer.put((byte) 0x61); //写入一个a字符
        debugAll(buffer);
        buffer.put(new byte[] {0x62,0x63,0x64});
        debugAll(buffer);
        //读取数据,需要做一个读模式的切换,将position位置归零
        buffer.flip();
        debugAll(buffer);
        byte b = buffer.get();
        System.out.println((char) b);
        debugAll(buffer);
        /*buffer.put((byte) 0x65); //字符e
        debugAll(buffer);
        byte b1 = buffer.get();
        System.out.println((char) b1);
        debugAll(buffer);*/
        /*buffer.clear();
        debugAll(buffer);
        byte b2 = buffer.get();
        System.out.println((char) b2);
        byte b3 = buffer.get();
        System.out.println((char) b3);*/
        buffer.compact();
        debugAll(buffer);
        buffer.put((byte) 0x66);
        debugAll(buffer);
        //get(int i)是可以重复读取数据,get后面不能使用compact方法,不然会导致位置错乱
        byte b4 = buffer.get(3);
        System.out.println((char) b4);
        debugAll(buffer);

    }
}
