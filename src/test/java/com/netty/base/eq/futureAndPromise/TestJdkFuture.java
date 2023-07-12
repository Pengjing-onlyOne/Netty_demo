package com.netty.base.eq.futureAndPromise;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class TestJdkFuture {
    //一般配合线程池使用
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //创建线程池
        ExecutorService pool = Executors.newFixedThreadPool(2);
        //提交任务
        Future<Object> future = pool.submit(new Callable<Object>() {
            @Override
            public Integer call() throws Exception {
                log.debug("开始计算");
                TimeUnit.SECONDS.sleep(5);
                return 1000;
            }
        });
        boolean cancelled = future.isCancelled();
        log.debug("是否取消{}",cancelled);
        //主线程通过future来获取结果
        log.debug("等待结果");
        Object o = future.get();
        log.debug("结果是{}",o);

    }
}
