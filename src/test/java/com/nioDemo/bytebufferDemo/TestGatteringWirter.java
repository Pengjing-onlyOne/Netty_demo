package com.nioDemo.bytebufferDemo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * @Description:
 * @Version: V1.0
 */
public class TestGatteringWirter {
    public static void main(String[] args) {
        //集中写,将多组数据一次性全部写入到一个文件中,减少数据的拷贝过程
        try (FileChannel channel = new FileOutputStream("words2.txt").getChannel()) {
            ByteBuffer b1 = StandardCharsets.UTF_8.encode("hello");
            ByteBuffer b2 = StandardCharsets.UTF_8.encode("world");
            ByteBuffer b3 = StandardCharsets.UTF_8.encode("你好,世界");
            channel.write(new ByteBuffer[]{b3,b1,b2});
        } catch (IOException e) {
        }
    }
}
