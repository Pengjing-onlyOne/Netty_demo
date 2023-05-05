package com.aioDemo;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static com.nioDemo.bytebufferDemo.ByteBufferUtil.debugAll;

/**
 * @Description:
 * @Version: V1.0
 */
@Slf4j
public class AIOFileChannel {
    public static void main(String[] args) throws IOException {
        try (AsynchronousFileChannel open = AsynchronousFileChannel.open(Paths.get("data.txt"), StandardOpenOption.READ)) {

            ByteBuffer buffer = ByteBuffer.allocate(16);

            /**
             * 参数1:ByteBuffer
             * 参数2:读取的位置
             * 参数3:附件,在参数1不够的情况下使用参数3
             * 参数4:回调对象 CompletionHandler
             */
            log.debug("read begin.......");
            open.read(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    log.debug("read complete......{}",result);
                    attachment.flip();
                    debugAll(attachment);
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    exc.printStackTrace();
                }
            });
            log.debug("read end........");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.in.read();

    }
}
