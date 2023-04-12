package com.nioDemo.nioWebSocket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugRead;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * 问题:对资源很浪费
 */
@Slf4j
public class ServerNoBlocking {
    public static void main(String[] args) throws IOException {
        //非阻塞模式的展示
        //0 ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);

        //1 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //开启ServerSocketChannel的非阻塞模式,影响的是ssc.accept()的代码
        ssc.configureBlocking(false);
        //2 绑定端口
        ssc.bind(new InetSocketAddress(8080));

        //3 连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while(true){
            //accept 建立客户端连接 SocketChannel 用来与客户端之间通信
            SocketChannel sc = ssc.accept();

            if(Optional.ofNullable(sc).isPresent()) {
                log.debug("connected.....{}", sc);

                //将SocketChannel设置以为非阻塞模式,影响的是读取数据的方法a.read(buffer);
                sc.configureBlocking(false); //非阻塞模式 线程还会继续运行,如果没有链接建立,但sc是null;

                channels.add(sc);
            }
            //遍历集合,获取消息
            channels.stream().forEach(a->{
                try {
                    int read = a.read(buffer);//非阻塞,线程仍会继续运行,如果没有读到数据,read会返回0
                    if(read >0) {
                        buffer.flip();
                        debugRead(buffer);
                        //切换到写模式,重置postiion
                        buffer.clear();
                        log.debug("after read....{}", a);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
