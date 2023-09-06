package com.pengjing.server.handler;

import com.pengjing.message.RpcResponseMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * rpc的响应处理器
 */

/**
 * 泛型通配符: <?> :只能在泛型容器中获取值,不能向泛型容器中设置值,可以存放null
 * Sharable:注解不能存在状态信息,需不需要添加主要是制作者考虑自行考虑能不能添加这个注解
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcResponseMessageHandler extends SimpleChannelInboundHandler<RpcResponseMessage> {

    //创建一个map用于存储请求方发送的消息
    //保证线程的安全性
    public static final Map<Integer, Promise<Object>> promises = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) throws Exception {
        log.debug("打印数据:{}",msg);

        //根据返回的对象填充promise对象
        //获取promise
        Promise<Object> promise = promises.get(msg.getSequenceId());

        if(promise != null){
            //成功的对象
            Object returnValue = msg.getReturnValue();
            //异常对象
            Exception exception = msg.getExceptionValue();

            if(exception != null){
                System.out.println("得到的异常为:"+exception);
                promise.setFailure(exception);
            }else {
                promise.setSuccess(returnValue);
            }
        };
        //处理完消息之后将消息移除
        promises.remove(msg.getSequenceId());
    }
}
