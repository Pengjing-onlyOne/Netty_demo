package com.nioDemo.bytebufferDemo;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class BufferTest {

    public static void main(String[] args) throws IOException {
        //获取FileChannel
        //获取方式有两种,一种输入输出流获取,RandomAccessFile

       //为了防止自己输入的时候出现各种意外,最好是使用程序来写一个文件,让他来读
        FileOutputStream fos = new FileOutputStream("nettydemo/channelDemo.txt");
        fos.write("1234567890123".getBytes());
        fos.close();
        try (FileChannel channel = new FileInputStream("nettydemo/channelDemo.txt").getChannel()) {
            while (true) {
                //准备缓冲区
                ByteBuffer byteBuffer = ByteBuffer.allocate(10);
                //读取数据
                int read = channel.read(byteBuffer);
                log.info("读取到的字节数{}",read);
                if(read == -1){
                    break;
                }
                //切换至读模式
                byteBuffer.flip();
                //将读取的数据写入日志
                while(byteBuffer.hasRemaining()){
                    byte b =  byteBuffer.get();
                    log.info("读取的数据是:{}",(char)b);
                }
                //切换为写模式
                byteBuffer.clear();
            }

        } catch (IOException e) {
        };
    }
}
