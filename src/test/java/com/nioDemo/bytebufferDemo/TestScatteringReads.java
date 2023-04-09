package com.nioDemo.bytebufferDemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugAll;

/**
 * @Description:
 * @Version: V1.0
 */
public class TestScatteringReads {
    public static void main(String[] args) {
        //分散读,将一个文件里面的数据,读取到多个bytebuffer中,读取文件的时候使用inputStream来读取文件里面的数据,如果使用的是outputSream就会导致读取的数据为空
        try (FileChannel channel = new FileInputStream("words.txt").getChannel()) {
            ByteBuffer b1 =ByteBuffer.allocate(3);
            ByteBuffer b2 =ByteBuffer.allocate(3);
            ByteBuffer b3 =ByteBuffer.allocate(5);
            channel.read(new ByteBuffer[]{b1,b2,b3});
            b1.flip();
            b2.flip();
            b3.flip();
            debugAll(b1);
            debugAll(b2);
            debugAll(b3);
        } catch (IOException e) {
        }
    }
}
