package com.nioDemo.nioWebSocket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugRead;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class Server2SelectorUseRead {
    public static void main(String[] args) throws IOException {
        //获取Selector
        Selector selector = Selector.open();

        //创建ServerSocketChannel
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //开启他的非阻塞模式
        ssc.configureBlocking(false);

        //将ServerSocketChannel注册到selector中
        SelectionKey ssckey = ssc.register(selector, 0, null);
        ssckey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("key:{}",ssckey);
        //绑定ServerSocketChannel的端口号
        ssc.bind(new InetSocketAddress(8080));

        while (true){
            selector.select();
            //获取selector中的key
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                log.debug("迭代器里面的key是:{}",key);
                //selector会在事件后,向selectedKeys集合中添加key,但是不删除,如果不删除,就会导致后续的循环一直在accept的if分支里面,从而会出现空指针异常
                iterator.remove();
                //判断key是属于什么事件
                if(key.isAcceptable()){
                    log.debug("========开始,sc.accept");
                    ServerSocketChannel channel = (ServerSocketChannel)key.channel();
                    SocketChannel sc = channel.accept();
                    sc.configureBlocking(false);
                    sc.register(selector,SelectionKey.OP_READ,null);
                    log.debug("{}",sc);
                }else if(key.isReadable()){
                    try {
                        log.debug("========开始,sc.read");
                        SocketChannel sc = (SocketChannel)key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        int read = sc.read(buffer);
                        //如果是正常断开,read的返回值就是-1
                        if(read == -1){
                            key.cancel();
                        }else {
                            log.debug("{}", sc);
                            buffer.flip();
                            debugRead(buffer);
                            buffer.clear();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        //因为客户端断开,需要将key取消(从selector的key中真正删除key)
                        key.cancel();
                    }
                }
            }
        }
    }
}
