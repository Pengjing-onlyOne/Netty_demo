package com.nioDemo.nioWebSocket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugRead;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        //使用nio理解阻塞模式 单线程
        //0 ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        //1 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();

        //2 绑定端口
        ssc.bind(new InetSocketAddress(8080));

        //3 连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while(true){
            //accept 建立客户端连接 SocketChannel 用来与客户端之间通信
            log.debug("connecting.......");
            //阻塞方法,线程停止运行
            SocketChannel channel = ssc.accept();

            log.debug("connected.....{}",channel);
            channels.add(channel);

            //遍历集合,获取消息
            channels.stream().forEach(a->{
                try {
                    //阻塞方法,线程停止运行
                    a.read(buffer);
                    log.debug("berfer read.......{}",a);
                    buffer.flip();
                    debugRead(buffer);
                    //切换到写模式,重置postiion
                    buffer.clear();
                    log.debug("after read....{}",a);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
