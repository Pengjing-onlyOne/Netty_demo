package com.nioDemo.fileChannel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @Description:
 * @Version: V1.0
 */
public class TestFilesWalkTreeCopy {
    public static void main(String[] args) throws IOException {
        //复制多级目录的文件
        String source = "/Users/pengjing/Downloads/文档";
        String target = "/Users/pengjing/Downloads/文档1";

            Files.walk(Paths.get(source)).forEach(path -> {
        try {
                //对文件夹进行替换
                String targetName = path.toString().replace(source, target);
                //创建目标文件夹
                if(Files.isDirectory(path)){
                    Files.createDirectory(Paths.get(targetName));
                }else if(Files.isRegularFile(path)){
                    //执行拷贝
                    Files.copy(path,Paths.get(targetName));
                }

        } catch (IOException e) {
            e.printStackTrace();
        }
            });
    }
}
