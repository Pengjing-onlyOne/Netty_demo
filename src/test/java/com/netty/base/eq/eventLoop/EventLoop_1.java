package com.netty.base.eq.eventLoop;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class EventLoop_1 {
    public static void main(String[] args) {
      //EventLoop的简单使用
        NioEventLoopGroup executors = new NioEventLoopGroup(3);//可以IO事件,普通任务,定时任务
        DefaultEventLoopGroup executors_default = new DefaultEventLoopGroup(3);//可以处理普通任务,定时任务

        //普通任务,因为EventLoopGroup继承了线程池的接口,可以使用线程池中的API
        executors.next().submit(()->{
            log.debug("这个能使用线程池中的方法");
        });

        //定时任务的使用
        executors.scheduleAtFixedRate(()->{
            log.debug("这个能使用定时任务的方法");
        },0,5, TimeUnit.SECONDS);

        System.out.println("main");
    }
}
