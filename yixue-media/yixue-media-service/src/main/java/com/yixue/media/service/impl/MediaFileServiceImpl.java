package com.yixue.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.yixue.base.exception.YixueException;
import com.yixue.base.model.PageParams;
import com.yixue.base.model.PageResult;
import com.yixue.media.mapper.MediaFilesMapper;
import com.yixue.media.mapper.MediaProcessMapper;
import com.yixue.media.model.dto.QueryMediaParamsDto;
import com.yixue.media.model.dto.RestResponse;
import com.yixue.media.model.dto.UploadFileParamsDto;
import com.yixue.media.model.dto.UploadFileResultDto;
import com.yixue.media.model.entity.MediaFiles;
import com.yixue.media.model.entity.MediaProcess;
import com.yixue.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Tavis
 * @description 媒资文件管理业务实现类
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Lazy//延迟加载（避免循环依赖问题）
    @Autowired//代理对象
    MediaFileService currentProxy;

    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    //存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    //查询媒资文件接口
    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());

    }

    //上传图片、文档文件接口
    @Override
    /*
       此方法进行了事物控制优化，即将@Transactional移至addMediaFilesToDb方法上，
       并注入了一个代理对象currentProxy（自己注入自己）调用addMediaFilesToDb方法
       原因：该方法涉及将文件存到MinIO中，与网络状况有关，因为运行时间在极限情况可能很长
       解释：在涉及数据库更改时要使用@Transactional注解，这样如果程序报错，
            就会发生事物回滚，当前这条错误数据就不会存入数据库
     */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        //获取文件名称
        String filename = uploadFileParamsDto.getFilename();
        //获取文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //获取文件类型
        String mimeType = getMimeType(extension);
        //获取当前日期（按照年/月/日存入minio）
        String nowDate = getNowDate();
        //获取文件md5值
        String fielMd5 = getFileMd5(new File(localFilePath));
        //拼装objectName
        String objectName = nowDate + fielMd5 + extension;
        //上传文件到minio
        boolean result = addMediafilesToMinio(localFilePath, mimeType, bucket_mediafiles, objectName);
        if (!result) {
            YixueException.cast("上传文件到MinIO失败");
        }
        //将文件信息保存到数据库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fielMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
        if (mediaFiles == null) {
            YixueException.cast("上传文件到数据库失败");
        }
        //准备返回的对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    //上传视频文件接口
    @Override
    public RestResponse uploadVideo(UploadFileParamsDto uploadFileParamsDto, String localFilePath) throws IOException {
        //获取文件md5值
        String fielMd5 = getFileMd5(new File(localFilePath));
        //将视频文件分块
        //源文件
        File sourceFile = new File(localFilePath);
        //分块文件本地存储路径（暂存）
        String chunkPath = "C:\\Users\\86183\\Desktop\\test\\chunk\\";
        //分块文件大小为5M
        int chunkSize = 1024 * 1024 * 5;
        //分块文件个数(向上取整)
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        //使用流从源文件读取数据
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        //缓冲区
        byte[] buffer = new byte[5120];
        for (int i = 0; i < chunkNum; i++) {
            //创建分块文件
            File chunkFile = new File(chunkPath + i);
            //使用流向分块文件写入数据
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
//        for (int i = 0; i < chunkNum; i++) {
//            //上传该分块
//            flag = uploadChunk(fielMd5, i, chunkPath);
//            //如果上传某一分块失败，则整个视频上传失败
//            if (!flag) {
//                return RestResponse.validfail(false, "上传视频文件失败");
//            }
//            System.out.println("上传第" + i + "块成功");
//        }
        //用于标记分块是否上传成功
        boolean flag = uploadChunk(fielMd5, chunkPath, chunkNum);
        return RestResponse.success(true);
    }

    //上传分块文件到minio
    public boolean uploadChunk(String fileMd5, String localFilePath, int chunkNum) {
        //分块文件的路径
        String chunkFilePath = getChunkFileFolderPath(fileMd5);
        boolean result = false;
        for (int i = 0; i < chunkNum; i++) {
            try {
                UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                        .bucket(bucket_video)
                        .object(chunkFilePath + i)//文件在minio中存储的路径
                        .filename(localFilePath + i)//指定本地文件路径
                        .build();
                //上传文件
                minioClient.uploadObject(uploadObjectArgs);
                System.out.println("上传分块" + i + "成功");
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
                result = false;
                System.out.println("上传分块" + i + "失败");
            }
        }
//        boolean b = addMediafilesToMinio(localFilePath, mimeType, bucket_video, chunkFileFolderPath);
//        if (b) {
//            //上传分块文件成功
//            return RestResponse.success(true);
//        }
//        //上传分块文件失败
//        return RestResponse.validfail(false, "上传分块文件失败");
        return result;
    }


    //上传图片、文档文件到minio
    public boolean addMediafilesToMinio(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)//文件在minio中存储的路径
                    .filename(localFilePath)//指定本地文件路径
                    .contentType(mimeType)
                    .build();
            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件成功,bucket:{},objectName:{}", bucket, objectName);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件失败,bucket:{},objectName:{},错误信息:{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    //检查文件是否存在接口
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //先查数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //桶
            String bucket = mediaFiles.getBucket();
            //文件存储名称
            String filePath = mediaFiles.getFilePath();
            //如果数据库中有该文件，再去minio中查看是否存在
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            //查询远程服务获取到一个流对象
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null) {
                    //文件已存在
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //文件不存在
        return RestResponse.validfail(false, "文件不存在");
    }

    //检查分块是否存在接口
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //分块存储路径：md5值的前两位为两个目录，chunk存储分块文件
        //根据md5值获取分块文件的目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream != null) {
                //分块文件已存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //分块文件不存在
        return RestResponse.validfail(false, "分块文件不存在");
    }

    //合并minio中的分块文件接口
    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //分块文件在minio中的存储路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //获取所有分块文件的信息
        List<ComposeSource> sources = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            //指定分块文件的信息
            ComposeSource source = ComposeSource.builder()
                    .bucket(bucket_video)
                    .object(chunkFileFolderPath + i)
                    .build();
            sources.add(source);
        }
        //源文件名称
        String filename = uploadFileParamsDto.getFilename();
        //扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //合并后文件的objectname
        String objectName = getFilePathByMd5(fileMd5, extension);
        //合并后的composeObjectArgs
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(objectName)//合并后的文件名
                .sources(sources)//指定源文件
                .build();
        //合并分块
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并分块失败,bucket:{},objectName:{},错误信息:{}", bucket_video, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并分块失败");
        }

        //校验合并后的文件与源文件是否一致
        //先下载合并后的文件
        File file = downloadFileFromMinIO(bucket_video, objectName);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            //计算合并后文件的md5
            String mergeFile_md5 = DigestUtils.md5Hex(fileInputStream);
            //比较原始和合并后的文件的md5值
            if (!mergeFile_md5.equalsIgnoreCase(fileMd5)) {
                log.error("文件校验失败,原始文件md5值:{},合并文件md5值:{}", fileMd5, mergeFile_md5);
                return RestResponse.validfail(false, "文件校验失败");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(file.length());
        } catch (Exception e) {
            return RestResponse.validfail(false, "文件校验失败");
        }
        //将文件信息入库
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件信息入库失败");
        }
        //清理分块文件
        clearChunkFiles(chunkFileFolderPath, chunkTotal);
        return RestResponse.success(true);
    }

    //根据md5值获取分块文件在minio中的存储路径
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }

    //得到合并后的文件的地址
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    //从minio下载文件
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    //清理分块文件
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                    .bucket(bucket_video)
                    .objects(deleteObjects)
                    .build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r -> {
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清楚分块文件失败,objectname:{}", deleteError.objectName(), e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清除分块文件失败,chunkFileFolderPath:{}", chunkFileFolderPath, e);
        }
    }

    //根据扩展名来获取文件类型
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    //获取当前日期 年/月/日
    private String getNowDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()).replace("-", "/") + "/";
    }

    //获取文件的md5值
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //将文件信息保存到数据库
    @Override
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            //文件id
            mediaFiles.setId(fileMd5);
            //机构类型
            mediaFiles.setCompanyId(companyId);
            //桶
            mediaFiles.setBucket(bucket);
            //文件存储名称
            mediaFiles.setFilePath(objectName);
            //文件id
            mediaFiles.setFileId(fileMd5);
            //url
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            //上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            //状态
            mediaFiles.setStatus("1");
            //审核状态
            mediaFiles.setAuditStatus("002003");
            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0) {
                log.debug("文件信息保存到数据库失败,bucket:{},objectName:{}", bucket, objectName);
                return null;
            }
            //记录待处理任务
            addWaitingTask(mediaFiles);
            return mediaFiles;
        }
        return mediaFiles;
    }

    //添加待处理任务
    private void addWaitingTask(MediaFiles mediaFiles) {
        //文件名称
        String fileName = mediaFiles.getFilename();
        //文件扩展名
        String extension = fileName.substring(fileName.lastIndexOf("."));
        //获取文件的mimetype
        String mimeType = getMimeType(extension);
        //如果是avi视频才写入待处理任务表
        if (mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");//未处理状态
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);//失败次数默认值为0
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }
    }

}
