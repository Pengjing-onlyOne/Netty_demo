package com.pengjing.server.handler;

import com.pengjing.message.RpcRequestMessage;
import com.pengjing.message.RpcResponseMessage;
import com.pengjing.service.HelloService;
import com.pengjing.service.ServicesFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * rpc消息处理器
 */
@Slf4j
@ChannelHandler.Sharable
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage rpcRequestMessage) throws Exception {
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        rpcResponseMessage.setSequenceId(rpcRequestMessage.getSequenceId());
        try {
            //使用反射的方法获取请求的对象和方法
            HelloService service = (HelloService)ServicesFactory.getService(Class.forName(rpcRequestMessage.getInterfaceName()));
            //根据class获取方法名
            Method method = service.getClass().getMethod(rpcRequestMessage.getMethodName(), rpcRequestMessage.getParameterTypes());
            Object invoke = method.invoke(service, rpcRequestMessage.getParameterValue());
//            rpcResponseMessage.setReturnValue(invoke);
            rpcResponseMessage.setExceptionValue(new Exception("远程调用出错:"));
        } catch (Exception e) {
            e.printStackTrace();
            //添加异常消息
            String msg = e.getCause().getMessage();
            rpcResponseMessage.setExceptionValue(new Exception("远程调用出错:"+msg));
        }
        ctx.writeAndFlush(rpcResponseMessage);
    }

    public static void main(String[] args) {
        RpcRequestMessage rpcRequestMessage = new RpcRequestMessage(1, "com.pengjing.service.HelloService", "sayHello", String.class, new Class[]{String.class}, new Object[]{"张三"});
        try {
            //使用反射的方法获取请求的对象和方法
            HelloService service = (HelloService)ServicesFactory.getService(Class.forName(rpcRequestMessage.getInterfaceName()));
            //根据class获取方法名
            Method method = service.getClass().getMethod(rpcRequestMessage.getMethodName(), rpcRequestMessage.getParameterTypes());
            Object invoke = method.invoke(service, rpcRequestMessage.getParameterValue());
            System.out.println(invoke);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
