package com.netty.update.stickyAndHalfWrapped;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @Description:
 * @Version: V1.0
 */
//redis的协议解析
public class TestRedis {
    /**
     * redis的协议
     * 如果向redis发一条命令
     * 首先发送数组的长度
     * set key value
     * 3
     * 每个命令每个键值的长度
     * eq set name zhangsan
     * 3
     * $3 命令的长度
     * set
     * $4 key的长度
     * name
     * $8 value的长度
     * zhangsan
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入redis服务器地址（回车直接本地服务器：localhost）");
        String hostname = scanner.nextLine();
        if (hostname.equals("")){
            hostname = "localhost";
        }
        int port;
        System.out.println("请输入redis端口(回车直接端口默认：6379)");
        while (true){
            try {
                String str = scanner.nextLine();
                if (str.equals("")){
                    port = 6379;
                }else {
                    port = Integer.parseInt(scanner.nextLine());
                }
                break;
            }catch (NumberFormatException e){
                System.out.println("请输入合法端口:"+e.getMessage());
            }
        }
        NioEventLoopGroup boss = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap()
                .group(boss)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel){
                        Scanner sc = new Scanner(System.in);
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) {
                                System.out.println("连接成功，请输入redis的命令指令");
                                String commend = sc.nextLine();
                                sendCommend(ctx, commend.trim());
                            }
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                ByteBuf byteBuf = (ByteBuf) msg;
                                String resp = byteBuf.toString(StandardCharsets.UTF_8);
//                                System.out.println("正常响应："+resp);
                                resp = resp.replace("\r\n","")
                                        .replace("+", "")
                                        .replace("$-1", "");
                                if (resp.contains("$")){
                                    resp = resp.substring(2);
                                }
                                System.out.println(resp);
                                if (resp.equals("-NOAUTH Authentication required.")){
                                    System.out.println("监测到连接该redis没有验证密码，请输入密码验证：");
                                    String password = sc.nextLine();
                                    String verifyCommend = "auth "+password;
                                    sendCommend(ctx, verifyCommend);
                                }else {
                                    String commend = sc.nextLine();
                                    sendCommend(ctx, commend.trim());
                                }
                            }
                        });
                    }
                });
        try {
            ChannelFuture connect = bootstrap.connect(new InetSocketAddress(hostname, port)).sync();
            connect.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
        }
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
