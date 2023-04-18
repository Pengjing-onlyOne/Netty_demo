package com.nioDemo.nioWebSocket.out_read.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Description:
 * @Version: V1.0
 */
public class WriteClient {
    public static void main(String[] args) throws IOException {
        SocketChannel sc  = SocketChannel.open();
        sc.connect(new InetSocketAddress("127.0.0.1",8080));
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        int read = 0;
        while(true) {
                read += sc.read(buffer);
            System.out.println(read);
            buffer.clear();
        }
    }
}
