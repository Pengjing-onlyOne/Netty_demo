package com.netty.base.eq.futureAndPromise;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class TestNettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //使用promise方法
        EventLoop eventLoop = new NioEventLoopGroup().next();

        DefaultPromise<String> promise = new DefaultPromise<>(eventLoop);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 1 / 0;
                    promise.setSuccess(400+"");
                } catch (Exception e) {
                    e.printStackTrace();
                    promise.setFailure(e);
                }

            }

        }).start();
        log.debug("等待结果.....");
//        promise.getNow();
        log.debug("得到的结果是:{}",promise.get());

    }
}
