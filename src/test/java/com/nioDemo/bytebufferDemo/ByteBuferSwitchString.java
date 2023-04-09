package com.nioDemo.bytebufferDemo;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugAll;

/**
 * @Description:
 * @Version: V1.0
 */
public class ByteBuferSwitchString {
    public static void main(String[] args) {
        //ByteBuffer和String的相互转换
        //使用String转换为ByteBuffer
        ByteBuffer buffer1 = ByteBuffer.allocate(16);
        buffer1.put("hello".getBytes());
        debugAll(buffer1);

        //使用chartset方法
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode("hello");
        debugAll(buffer2);

        //使用nio的方法
        ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());
        debugAll(buffer3);

        //ByteBuffer转换为String
        String str2 = StandardCharsets.UTF_8.decode(buffer2).toString();
        System.out.println("转换的数据是:"+str2);

        String str3 = StandardCharsets.UTF_8.decode(buffer3).toString();
        System.out.println("转换的数据是:"+str3);

        //转换第一个方法会出现问题,他的下标还是在写入时候的位置,需要把position的位置初始化
        buffer1.flip();
        String str1 = StandardCharsets.UTF_8.decode(buffer1).toString();
        System.out.println("转换的数据是:"+str1);

    }
}
