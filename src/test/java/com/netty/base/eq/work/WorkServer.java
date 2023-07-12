package com.netty.base.eq.work;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description:
 * @Version: V1.0
 */
//双向通信,客户端发送什么给服务端,服务端也发送什么给客户端
@Slf4j
public class WorkServer {
    public static void main(String[] args) throws InterruptedException {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //首先将数据格式化
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new StringDecoder());
                        //创建出站和入站处理
                        pipeline.addLast("l1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("读取的消息是:"+msg.toString());
//                                ctx.writeAndFlush(msg.toString());
                                String s = msg.toString();
                                super.channelRead(ctx, s);
                            }
                        });
                        pipeline.addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("发送的消息是:"+msg);
                                ctx.writeAndFlush(ctx.alloc().buffer().writeBytes(msg.toString().getBytes()));
                            }
                        });
                        /*pipeline.addLast("w1",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//                                super.write(ctx, msg, promise);
                                log.debug("发送的消息是:"+msg);
                                ctx.writeAndFlush(msg);
                            }
                        });*/
                    }
                }).bind(8080).sync().channel().read();
    }
}
