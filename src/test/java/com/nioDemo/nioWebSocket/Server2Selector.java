package com.nioDemo.nioWebSocket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class Server2Selector {
    public static void main(String[] args) throws IOException {
        //1 创建 Selector 管理多个channel
        Selector selector = Selector.open();


        ByteBuffer buffer = ByteBuffer.allocate(16);

        //1 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        //使用selector的时候,需要将ServerSocketChannel开启非阻塞模式
        ssc.configureBlocking(false);
        //2 建立Selector 和channel的联系(注册)
        /**
         * 事件有多个类型
         * 1.accpet  --会在有连接请求时触发
         * 2.connect --是客户端连接建立后触发
         * 3.read --读取客户端发送的信息后触发
         * 4.write --可写事件
         */
        SelectionKey ssckey = ssc.register(selector, 0, null);
        //key只关注accept事件
        ssckey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("regeister key:{}",ssckey);

        ssc.bind(new InetSocketAddress(8080));

        while(true){
           //3select方法 没有事件发生,线程阻塞,有事件发生,线程恢复运行
            selector.select();
            //4 处理事件 selectedKeys 内部包含了所有发生的事件 获取所有的可读可写的事件的集合
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while(iterator.hasNext()){
                SelectionKey key = iterator.next();
                log.debug("key,{}",key);
                //取消任务
                key.cancel();
            }
        }
    }
}
