package com.yixue.media;

import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.util.*;


/**
 * @author Tavis
 * @date 2023-08-15
 * @desc 测试大文件上传方法
 */
public class VideoFilesTest {

    //测试分块
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("C:\\Users\\86183\\Desktop\\test\\1.mp4");
        //分块文件存储路径
        String chunkPath = "C:\\Users\\86183\\Desktop\\test\\chunk\\";
        //分块文件大小为5M
        int chunkSize = 1024 * 1024 * 5;
        //分块文件个数(向上取整)
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        //使用流从源文件读取数据，向分块文件写入数据
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        //缓冲区
        byte[] buffer = new byte[5120];
        for (int i = 0; i < chunkNum; i++) {
            //创建分块文件
            File chunkFile = new File(chunkPath + i);
            //分块文件写入流
            RandomAccessFile raf_write = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = raf_read.read(buffer)) != -1) {
                raf_write.write(buffer, 0, len);
                //如果块文件大小达到5M，开始写下一块
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }
            raf_write.close();
        }
        raf_read.close();
    }

    //测试合并
    @Test
    public void testMerge() throws IOException {
        //分块文件目录
        File chunkFolder = new File("C:\\Users\\86183\\Desktop\\test\\chunk\\");
        //源文件
        File sourceFile = new File("C:\\Users\\86183\\Desktop\\test\\1.mp4");
        //合并文件
        File mergeFile = new File("C:\\Users\\86183\\Desktop\\test\\merge\\2.mp4");
        //取出所有分块文件
        File[] files = chunkFolder.listFiles();
        //将数组转成list
        List<File> fileList = Arrays.asList(files);
        //对分块文件升序排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override//排序规则：升序
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        //向合并文件写入的流
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        //缓冲区
        byte[] buffer = new byte[5120];
        //遍历分块文件，将分块文件写入合并文件
        for (File chunkFile : fileList) {
            //读取分块文件
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
            int len = -1;
            while ((len = raf_read.read(buffer)) != -1) {
                raf_write.write(buffer, 0, len);
            }
            raf_read.close();
        }
        raf_write.close();
        //校验合并文件（对比md5值是否相同）
        FileInputStream mergeFileInput = new FileInputStream(mergeFile);
        FileInputStream sourceFileInput = new FileInputStream(sourceFile);
        String mergeFileMd5 = DigestUtils.md5DigestAsHex(mergeFileInput);
        String sourceFileMd5 = DigestUtils.md5DigestAsHex(sourceFileInput);
        if (mergeFileMd5.equals(sourceFileMd5)) {
            System.out.println("合并成功");
        } else {
            System.out.println("合并失败");
        }
    }

}
