package com.netty.update.stickyAndHalfWrapped;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @Description:
 * @Version: V1.0
 */
//简易的redis连接工具,可以执行简易的操作
@Slf4j
public class MyRedisTest {
    public static void main(String[] args) throws InterruptedException {
        //输入相关的连接信息
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入你要连接的redis地址(如果是localhost直接返回)");
        String url = scanner.nextLine();
        if(StringUtil.isNullOrEmpty(url.trim())){
            url = "127.0.0.1";
        }
        System.out.println("请输入你要连接的redis的地址(默认地址直接返回)");
        String port = scanner.nextLine();
        if(StringUtil.isNullOrEmpty(port.trim())){
            port = "6379";
        }
        int port_int;
        while(true) {
            try {
                 port_int = Integer.parseInt(port);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("输入的端口号不合法,请重新输入");
            }
        }

        //连接相关的redis
        NioEventLoopGroup bossEvent = new NioEventLoopGroup();
        NioEventLoopGroup workerEvent = new NioEventLoopGroup(2);
        Channel channel = new Bootstrap().group(bossEvent).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                Scanner scanner_redis = new Scanner(System.in);
                //配置日志
                socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//                                super.channelActive(ctx);
                        //创建消息,并将消息发送给redis
                        System.out.println("请输入你要发送的命令");
                        String msg = scanner_redis.nextLine();
                        sendCommend(ctx, msg.trim());
                    }

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        //读取相关消息
                        ByteBuf byteBuf = (ByteBuf) msg;
                        String resp = byteBuf.toString(StandardCharsets.UTF_8);
                        if ((resp.toString()).contains("NOAUTH Authentication required")) {
                            //连接的redis需要密码
                            System.out.println("连接的redis需要密码,请输入你的密码");
                            String password = scanner_redis.nextLine();
                            String verifyCommend = "auth " + password;
                            sendCommend(ctx, verifyCommend);
                        } else {
                            String commend = scanner_redis.nextLine();
                            sendCommend(ctx, commend.trim());
                        }
//                                super.channelRead(ctx, msg);
                    }
                });

            }
        }).connect(new InetSocketAddress(url, port_int)).sync().channel();
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                bossEvent.shutdownGracefully();
            }
        });
    }


    private static void sendCommend(ChannelHandlerContext ctx, String commend) {
        //        空格+换行
        final byte[] LINE = {13,10};
        if (commend.equals("q")){
            System.out.println("正在退出程序...");
            ctx.close();
        }
        List<String> words = Arrays.asList(commend.split(" "));
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(("*" + words.size()).getBytes(StandardCharsets.UTF_8));
        buffer.writeBytes(LINE);
        for (String word : words) {
//            判断是不是中文汉字，如果是汉字，则每个汉字在utf-8 编码中占三个字节
            if (checkChinese(word)){
//                所以每个汉字的字节数*3
                buffer.writeBytes(("$" + word.length()*3).getBytes(StandardCharsets.UTF_8));
            }else {
                buffer.writeBytes(("$" + word.length()).getBytes(StandardCharsets.UTF_8));
            }
            buffer.writeBytes(LINE);
            buffer.writeBytes(word.getBytes(StandardCharsets.UTF_8));
            buffer.writeBytes(LINE);
        }
        ctx.writeAndFlush(buffer);
    }

    public static boolean checkChinese(String name)
    {
        int n;
        for(int i = 0; i < name.length(); i++) {
            n = name.charAt(i);
            if(!(19968 <= n && n <40869)) {
                return false;
            }
        }
        return true;
    }
}
