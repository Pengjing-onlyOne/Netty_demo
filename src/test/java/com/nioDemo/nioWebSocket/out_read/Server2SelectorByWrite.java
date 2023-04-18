package com.nioDemo.nioWebSocket.out_read;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @Description:
 * @Version: V1.0
 */
public class Server2SelectorByWrite {
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel ssc  = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(8080));
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT,null);


        while(true){
            selector.select();

            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                iterator.remove();
                if(key.isAcceptable()){

                    ServerSocketChannel ssckey =(ServerSocketChannel) key.channel();
                    SocketChannel sc = ssckey.accept();

                    sc.configureBlocking(false);

                    SelectionKey sckey = sc.register(selector, 0, null);

                    //向客户端发送大量数据
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 3000000; i++) {
                        sb.append("a");
                    }

                    ByteBuffer buffer = StandardCharsets.UTF_8.encode(sb.toString());
                        int writer = sc.write(buffer);
                        System.out.println(writer);
                        //判断是否还有数据没有写完
                        if(buffer.hasRemaining()){
                            //关注可写事件 读事件是1 写事件是2
                            sckey.interestOps(sckey.interestOps() + SelectionKey.OP_WRITE);
                            sckey.attach(buffer);
                        }
                }else if(key.isWritable()){
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    int write = socketChannel.write(buffer);
                    System.out.println(write);

                    //清理操作
                    if(!buffer.hasRemaining()){
                        //清除buffer
                        key.attach(null);
                        //数据写完不需要关注可写事件
                        key.interestOps(key.interestOps() - SelectionKey.OP_WRITE);
                    }
                }
            }
        }
    }
}

/**
 * 控制台打印的结果:
 * 482340
 * 220820
 * 679468
 * 802036
 * 637884
 * 177452
 */
