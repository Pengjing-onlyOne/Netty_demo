package com.nioDemo.nioWebSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @Description:
 * @Version: V1.0
 */
public class Client {
    public static void main(String[] args) throws IOException {
        //创建客户端对象
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost",8080));
        System.out.println("waiting.......");
        sc.write(StandardCharsets.UTF_8.encode("1234567890abc"));
        System.in.read();
    }
}
