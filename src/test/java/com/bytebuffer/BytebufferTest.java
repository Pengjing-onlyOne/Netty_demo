package com.bytebuffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugAll;

/**
 * @Description:
 * @Version: V1.0
 */
public class BytebufferTest {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put("1".getBytes(StandardCharsets.UTF_8));
        buffer.put("2".getBytes(StandardCharsets.UTF_8));
        buffer.put("3".getBytes(StandardCharsets.UTF_8));
        debugAll(buffer);
        buffer.flip();
        buffer.compact();
        buffer.put("4".getBytes());
        System.out.println("加数之后的结果是");
        debugAll(buffer);
    }
}
