package com.rpc.manager;

import com.pengjing.message.RpcRequestMessage;
import com.pengjing.protocol.MessageDecodecSharble;
import com.pengjing.protocol.ProcotolFrameDecoder;
import com.pengjing.server.handler.RpcResponseMessageHandler;
import com.pengjing.service.HelloService;
import com.rpc.sequence.SequenceIdGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class RpcClientManager {


    //模拟消息发送
    public static void main(String[] args) {
        HelloService helloService = getProxyService(HelloService.class);
        System.out.println(helloService.sayHello("zhangsan"));
        System.out.println(helloService.sayHello("lisi"));
        System.out.println(helloService.sayHello("wangwu"));
    }

    public static <T> T getProxyService(Class<T> serviceClass){
        //使用代理的方式发送数据
        ClassLoader classloader = serviceClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{serviceClass};
        //1.将方法调用转化为消息对象
        Object o = Proxy.newProxyInstance(classloader, interfaces, (proxy, method, args) -> {

            int sequenceId = SequenceIdGenerator.nextId();
            //创建一个获取序列号的方式
            RpcRequestMessage msg = new RpcRequestMessage(sequenceId,
                    serviceClass.getName(),
                    method.getName(),
                    method.getReturnType(),
                    method.getParameterTypes(),
                    args);

            //将消息发送
            createChannel().writeAndFlush(msg);
            DefaultPromise promise = new DefaultPromise(createChannel().eventLoop());
            RpcResponseMessageHandler.promises.put(sequenceId,promise);

            //等待结果的返回,无论有没有都会返回并且不会报异常,sync会抛异常
            promise.await();
            //对象的返回使用的是一个promise方式
            if(promise.isSuccess()){
                System.out.println(".........................");
                return promise.getNow();
            }else {
                throw new RuntimeException(promise.cause());
            }
//            return null;
        });
        return (T) o;
    }

    //创建一个代理类,实现远程调用的接口

    private static Channel channel = null;
    //添加一个锁
    private static  final  Object LOCK = new Object();
    //获取channel方法
    public static Channel createChannel() {
        if(channel != null){
            return channel;
        }
        synchronized(LOCK){
            if(channel != null){
                return  channel;
            }else {
                 initChannel();
                 return channel;
            }
        }
    }

    private static void initChannel() {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        //日志打印
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        //消息的编解码
        MessageDecodecSharble MESSAGE_HANDLER = new MessageDecodecSharble();
        //rpc请求处理
        RpcResponseMessageHandler RPC_RESPONSE = new RpcResponseMessageHandler();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //解决消息黏包半包
                ch.pipeline().addLast(new ProcotolFrameDecoder());
                //打印日志
                ch.pipeline().addLast(LOGGING_HANDLER);
                //消息编解码
                ch.pipeline().addLast(MESSAGE_HANDLER);
                //处理rpc消息
                ch.pipeline().addLast(RPC_RESPONSE);
            }
        });
        try {
             channel = bootstrap.connect(new InetSocketAddress("127.0.0.1", 8080)).sync().channel();
            channel.closeFuture().addListener(future->{
                eventExecutors.shutdownGracefully();
            });
        } catch (InterruptedException e) {
            log.error("client error",e.getMessage());
        }
    }
}
