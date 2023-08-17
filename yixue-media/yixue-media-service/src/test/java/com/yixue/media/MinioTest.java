package com.yixue.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-12
 * @desc 测试minio的SDK
 */
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://localhost:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test//测试上传文件功能
    public void testUpload() throws Exception {

        //通过扩展名得到媒体资源类型mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".jpg");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }

        //上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")//桶
                .filename("C:\\Users\\86183\\Desktop\\1.jpg")//指定本地文件路径
                //.object("1.jpg")//对象名(在桶下存储文件)
                .object("test/01/1.jpg")//文件在minio中存储的路径（一个/表示一层包）
                .contentType(mimeType)//设置媒体文件类型
                .build();
        //执行上传文件
        minioClient.uploadObject(uploadObjectArgs);
    }

    @Test//测试删除文件功能
    public void testDelete() throws Exception {

        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("video")//桶
                .object("e/3/e3469292a4ba5ea779a6642927f9fba3/e3469292a4ba5ea779a6642927f9fba3.mp4")//对象名
                .build();
        //执行删除文件
        minioClient.removeObject(removeObjectArgs);
    }

    @Test//测试查询文件功能：从minio中下载文件
    public void testGetFile() throws Exception {

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")//桶
                .object("test/01/1.jpg")//文件在minio中存储的路径
                .build();
        //查询远程服务器获取到一个流对象
        FilterInputStream fileInputStream = minioClient.getObject(getObjectArgs);
        //指定输出流(设置文件下载到本地的位置)
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\86183\\Desktop\\2.jpg");
        IOUtils.copy(fileInputStream, fileOutputStream);

        //得到原文件的md5值
        FileInputStream fileInputStream1 = new FileInputStream("C:\\Users\\86183\\Desktop\\1.jpg");
        String source_md5 = DigestUtils.md5Hex(fileInputStream1);
        //得到下载文件的md5值
        FileInputStream fileInputStream2 = new FileInputStream("C:\\Users\\86183\\Desktop\\2.jpg");
        String local_md5 = DigestUtils.md5Hex(fileInputStream2);
        //校验文件的完整性对文件的内容进行md5校验
        if (source_md5.equals(local_md5)) {
            System.out.println("下载成功");
        }
    }

    //将分块文件上传到minio
    @Test
    public void uploadChunk() throws Exception {
        for (int i = 0; i < 2; i++) {
            //上传文件的参数信息
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")//桶
                    .object("chunk/" + i)//文件在minio中存储的路径
                    .filename("C:\\Users\\86183\\Desktop\\test\\chunk\\" + i)//指定本地文件路径
                    .build();
            //执行上传文件
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传分块" + i + "成功");
        }
    }

    //调用minio接口合并分块(要求分块最小为5M)
    @Test
    public void testMerge() throws Exception {

        List<ComposeSource> sources = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            //指定分块文件的信息
            ComposeSource source = ComposeSource.builder()
                    .bucket("testbucket")
                    .object("chunk/" + i)//文件在minio中存储的路径
                    .build();
            //将分块文件的信息添加到集合中
            sources.add(source);
        }

        //指定合并后文件在minio中存储的路径
        ComposeObjectArgs testbucket = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge/merge01.mp4")//文件在minio中存储的路径
                .sources(sources)//指定源文件
                .build();
        //合并分块
        minioClient.composeObject(testbucket);
        System.out.println("合并成功");
    }
}
