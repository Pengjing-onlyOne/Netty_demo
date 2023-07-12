package com.netty.base.eq.futureAndPromise;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //需要一个线程池
        NioEventLoopGroup group = new NioEventLoopGroup();
        Future<Integer> future = group.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("开始执行运算");
                TimeUnit.SECONDS.sleep(5);
                return 1000;
            }
        });

        log.debug("等待结果");
        Integer integer = future.get();
        log.debug("结果是{}",integer);
    }
}
