package com.pengjing.server.handler;

import com.pengjing.message.RpcRequestMessage;
import com.pengjing.message.RpcResponseMessage;
import com.pengjing.service.HelloService;
import com.pengjing.service.ServicesFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

/**
 * @Description:
 * @Version: V1.0
 */

/**
 * rpc消息处理器
 */
public class RpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage rpcRequestMessage) throws Exception {
        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage();
        try {
            //使用反射的方法获取请求的对象和方法
            HelloService service = (HelloService)ServicesFactory.getService(Class.forName(rpcRequestMessage.getInterfaceName()));
            //根据class获取方法名
            Method method = service.getClass().getMethod(rpcRequestMessage.getMethodName(), rpcRequestMessage.getParameterTypes());
            Object invoke = method.invoke(service, rpcRequestMessage.getParameterValue());
            rpcResponseMessage.setReturnValue(invoke);
        } catch (Exception e) {
            e.printStackTrace();
            rpcResponseMessage.setExceptionValue(e);
        }
        rpcResponseMessage.setSequenceId(rpcRequestMessage.getSequenceId());
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
