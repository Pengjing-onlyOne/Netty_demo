package com.netty.update.stickyAndHalfWrapped.fixLength;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Random;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * 解决黏包和半包的几种思路
 * 1.使用短连接
 * 2.在服务器上根据客户端上发送的最大的数据大小设置定长接收
 */
@Slf4j
public class SlickClient {
    /*public static void main(String[] args) throws InterruptedException {
        *//*使用短连接,每次在消息发送完之后就将连接关闭*//*
        for (int i = 0; i < 10; i++) {
            send();
        }
        System.out.println("发送完毕");
    }

    private static void send() throws InterruptedException {
        try {
            NioEventLoopGroup clientEvent = new NioEventLoopGroup();
            Channel connect = null;
            Bootstrap bootstrap= new Bootstrap().group(clientEvent).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        *//*会在连接建立的时候出发active事件*//*
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buf = ctx.alloc().buffer(16);
                            buf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,});
                            ctx.writeAndFlush(buf);
                        }
                    });
                }
            });
             connect = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
             connect.close();
             clientEvent.shutdownGracefully();
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/
    public static void main(String[] args) throws InterruptedException {
        new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                Random random = new Random();
                                System.out.println(random.nextInt(11));
                                for (int i = 0; i < 10; i++) {
                                    ByteBuf buf = createByte((char)i,random.nextInt(11));
                                    ctx.writeAndFlush(buf);
                                }
                            }
                        });
                    }
                }).connect(new InetSocketAddress("127.0.0.1",8080)).sync();
    }

    public static ByteBuf createByte(char i,int len){
        ByteBuf byteBuf = Unpooled.buffer(len);
        for (int i1 = 0; i1 < 10; i1++) {
            if(i1<=len) {
                byteBuf.writeByte(i);
            }else {
                byteBuf.writeByte('-');
            }
        }
        return byteBuf;
    }
}
