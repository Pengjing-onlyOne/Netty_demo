package com.nioDemo.bytebufferDemo;

import java.nio.ByteBuffer;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugAll;

/**
 * @Description:
 * @Version: V1.0
 */
public class ByteBufferProjcet {
    /**
     * 网络上有多条数据发送到服务端,数据之间使用\n进行分隔,但由于某种原因
     * 这些数据在接收时,被进行了重新组合,例如原始的三条为
     * hello,world\n
     * I'm zhangsan\n
     * How are you?\n
     * 变成下面的两个bytebuffer(黏包,半包)
     * 黏包:一次发送多条数据,导致数据可能黏在一起
     * 半包:超过接收容量,导致数据到了下一个的容器里面
     * hello,world\nI'm zhangsan\nHo
     * w are you?\n
     * 要求编写程序,将错乱的数据恢复成原始的按\n分隔数据
     */
    public static void main(String[] args) {
        //创建两个bytebuffer.模拟出现的错乱的数据
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("hello,world\nI'm zhangsan\nHo".getBytes());
        split(source);
        source.put("w are you ?\n".getBytes());
        split(source);

    }

    private static void split(ByteBuffer source){
//        debugAll(source);
        //切换为读模式,将position初始化
        source.flip();
        //将bytebuffer循环获取字符
        for(int i = 0 ; i < source.limit() ; i++){
            //在字符等于\n的时候表示是一条完整的数据
            if(source.get(i) == '\n') {
                //获取他的长度,用于创建bytebuffer
                int length = i+1-source.position();
                //创建一个byteBuffer获取数据
                ByteBuffer target = ByteBuffer.allocate(length);
                for(int j = 0 ; j< target.limit();j++){
                    target.put(source.get());
                }
                debugAll(target);
            }
        }
        source.compact();
    }

}
