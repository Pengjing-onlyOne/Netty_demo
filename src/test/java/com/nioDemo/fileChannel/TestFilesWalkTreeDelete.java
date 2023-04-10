package com.nioDemo.fileChannel;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @Description:
 * @Version: V1.0
 */
public class TestFilesWalkTreeDelete {
    public static void main(String[] args) throws IOException {
//        Files.deleteIfExists(Paths.get("/Users/pengjing/Downloads/nacos-2.2.1-RC"));
        //逐级删除目录
        Files.walkFileTree(Paths.get("/Users/pengjing/Downloads/nacos-2.2.1-RC"),new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                Files.deleteIfExists(file);
                System.out.println("删除了======>"+file.toString());
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                System.out.println("得到的文件夹是:"+dir);
                Files.deleteIfExists(dir);
                System.out.println("删除的文件夹是:"+dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }
}
