package com.netty.update.stickyAndHalfWrapped;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class TestHttp {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup bossEvent = new NioEventLoopGroup();
        NioEventLoopGroup workEvent = new NioEventLoopGroup(2);
        Channel channel =  new ServerBootstrap()
                .group(bossEvent,workEvent)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        //表示http的编解码,既是入栈处理器,也是出站处理器
                        socketChannel.pipeline().addLast(new HttpServerCodec());
                        //在HttpServerCodec的返回中会存在DefaultHttpRequest和EmptyLastHttpContent两个对象,无论是什么请求都会有两个
                        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<HttpRequest>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpRequest httpRequest) throws Exception {
                                //可以使用SimpleChannelInboundHandler方法来接收自己需要的请求,减少if else的出现
                                //可以返回给浏览器数据使用
                                DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(httpRequest.protocolVersion(), HttpResponseStatus.OK);
                                //让浏览器知道内容发送完毕,需要给出返回体的长度
                                byte[] bytes = "<h1>hello ,world</h1>".getBytes();
                                defaultFullHttpResponse.headers().setInt(CONTENT_LENGTH,bytes.length);
                                //返回给浏览器的内容
                                defaultFullHttpResponse.content().writeBytes(bytes);

                                channelHandlerContext.writeAndFlush(defaultFullHttpResponse);

                            }
                        });
                        /*socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("{}",msg);
                            }
                        });*/
                    }
                }).bind(8080).sync().channel();

//        channel.close();
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                bossEvent.shutdownGracefully();
                workEvent.shutdownGracefully();
            }
        });
    }
}
