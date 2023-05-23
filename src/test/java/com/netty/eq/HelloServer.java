package com.netty.eq;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class HelloServer {
    public static void main(String[] args) {
        //1.启动器，负责组装netty组件，启动服务器
        new ServerBootstrap()
                //2.BossEventLoop,WorkerEventLoop(包含线程和选择器)
                .group(new NioEventLoopGroup())
                //3.选择服务器的serverSocketChannel实现
                //oioserverSocketChannel是阻塞的io实现
                .channel(NioServerSocketChannel.class)
                //4.boss，负责处理连接，worker负责处理读写，
                //决定将来能做什么事（能执行哪些操作（handle））
                .childHandler(
                        //5.代表和客户端进行数据读写的通道
                        new ChannelInitializer<NioSocketChannel>() {
                    //6.初始化器，负责添加别的处理器
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //7.添加具体的handle
                        nioSocketChannel.pipeline().addLast(new StringDecoder());//将传来的ByteBuffer转化为字符串
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){//自定义的业务处理
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                super.channelRead(ctx, msg);
                                //打印上一步转换好的字符串
                                System.out.println(msg);
                            }
                        });
                    }
                }).bind(8080);//绑定的监听端口
    }
}
