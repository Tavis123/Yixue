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
                .object("test/01/1.jpg")//对象名(在子目录test/01中存储文件)
                .contentType(mimeType)//设置媒体文件类型
                .build();
        //执行上传文件
        minioClient.uploadObject(uploadObjectArgs);
    }

    @Test//测试删除文件功能
    public void testDelete() throws Exception {

        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("testbucket")//桶
                .object("1.jpg")//对象名
                .build();
        //执行删除文件
        minioClient.removeObject(removeObjectArgs);
    }

    @Test//测试查询文件 从minio中下载文件
    public void testGetFile() throws Exception {

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")//桶
                .object("test/01/1.jpg")//对象名
                .build();
        //查询远程服务器获取到一个流对象
        FilterInputStream fileInputStream = minioClient.getObject(getObjectArgs);
        //指定输出流(设置文件下载到本地的位置)
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\86183\\Desktop\\2.jpg");
        IOUtils.copy(fileInputStream, fileOutputStream);

        //校验文件的完整性对文件的内容进行md5校验(对比原文件和下载文件的md5值)
        FileInputStream fileInputStream1 = new FileInputStream("C:\\Users\\86183\\Desktop\\1.jpg");
        String source_md5 = DigestUtils.md5Hex(fileInputStream1);
        FileInputStream fileInputStream2 = new FileInputStream("C:\\Users\\86183\\Desktop\\2.jpg");
        String local_md5 = DigestUtils.md5Hex(fileInputStream2);
        if (source_md5.equals(local_md5)) {
            System.out.println("下载成功");
        }
    }
}
