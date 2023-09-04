package com.rpc.sequence;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class SequenceIdGenerator {
    private static final AtomicInteger squenceId = new AtomicInteger();

    public  static int nextId(){
        return squenceId.incrementAndGet();
    }


}
