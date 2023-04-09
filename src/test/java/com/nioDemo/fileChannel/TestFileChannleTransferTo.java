package com.nioDemo.fileChannel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @Description:
 * @Version: V1.0
 */
public class TestFileChannleTransferTo {
    public static void main(String[] args) {
        try (
                FileChannel from = new FileInputStream("words2.txt").getChannel();
                FileChannel to = new FileOutputStream("words3.txt").getChannel()
            ) {
            //效率高,底层会利用操作系统的零拷贝进行优化,最多传2G的数据
//            from.transferTo(0,from.size(),to);
            //传输高于2G的文件方式
            //获取传输文件的大小
            long size = from.size();
            for(long left = size;left>0;){
                left =left - from.transferTo((size-left),left,to);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
