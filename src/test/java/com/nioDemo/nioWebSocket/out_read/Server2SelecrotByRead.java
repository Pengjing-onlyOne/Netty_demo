package com.nioDemo.nioWebSocket.out_read;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugAll;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class Server2SelecrotByRead {
    //使用不同的方式处理消息的读
    //1.使用分隔符发送消息,在服务端切割分隔符还获取消息

    public static void main(String[] args) throws IOException {
        //1.获取selector
        Selector selector = Selector.open();

        //2.创建ServerSocketChannel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        log.debug("得到的ssc为{}",ssc);
        //开启它的非阻塞模式
        ssc.configureBlocking(false);

        //绑定端口
        ssc.bind(new InetSocketAddress(8080));

        //注册到选择器中
        SelectionKey selectionKey = ssc.register(selector, SelectionKey.OP_ACCEPT, null);
        log.debug("得到的selectionkey是:{}",selectionKey);

        while(true){
            //使用selector的select获取需要处理的事件
            selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            //使用迭代器获取需要处理的事件
            if(iterator.hasNext()){
                //只要是有就说明存在需要处理的事件
                SelectionKey key = iterator.next();
                log.debug("d得到的key是:{}",key);
                //将key移除
                iterator.remove();

                //判断key的处理事件的模式是什么
                if(key.isAcceptable()){
                    //如果是接收事件,就开始接收的操作
                    ServerSocketChannel ssckey =(ServerSocketChannel) key.channel();
                    ssckey.configureBlocking(false);
                    ByteBuffer buffer = ByteBuffer.allocate(1<<4);
                    //第三个参数,表示的是附件,作为附件关联到selectionkey上
                    SocketChannel sc = ssckey.accept();
                    sc.configureBlocking(false);
                    sc.register(selector,SelectionKey.OP_READ,buffer);
                }else if(key.isReadable()){
                    try{
                    //读事件
                    SocketChannel sckey =(SocketChannel) key.channel();
                    //获取selectiokey上关联的附件
                    ByteBuffer buffer =(ByteBuffer) key.attachment();
                    int read = sckey.read(buffer);
                    debugAll(buffer);
                    if(read == -1){
                        key.cancel();
                    }
                    splite(buffer);
                    //如果buffer的position和limite长度一样,就表示需要扩容
                        if(buffer.position() == buffer.limit()){
                            //扩容为之前的两倍
                            ByteBuffer newBytebuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                            buffer.flip();
                            newBytebuffer.put(buffer);
                            key.attach(newBytebuffer);
                        }

                } catch (Exception e){
                    e.printStackTrace();
                    key.cancel();
                    }
                }
            }
        }
    }

    private static void splite(ByteBuffer buffer) {
        buffer.flip();
        int limit = buffer.limit();
        for(int i=0;i<limit;i++){
            if(buffer.get(i)=='\n'){
                //这是一条完整的数据
                int length = i+1-buffer.position();
                ByteBuffer targer = ByteBuffer.allocate(length);
                for(int j = 0 ; j< targer.limit();j++){
                    targer.put(buffer.get());
                }
                debugAll(targer);
            }
        }
        buffer.compact();
    }

}
