package com.yixue.media.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yixue.base.utils.Mp4VideoUtil;
import com.yixue.media.model.entity.MediaProcess;
import com.yixue.media.service.MediaFileProcessService;
import com.yixue.media.service.MediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 视频处理任务类
 */
@Slf4j
@Component
public class VideoTask {

    //从nacos中获取配置ffmpeg的路径
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;

    //视频处理任务
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {
        // 执行器序号，从0开始
        int shardIndex = XxlJobHelper.getShardIndex();
        //执行器总数
        int shardTotal = XxlJobHelper.getShardTotal();

        //确定CPU的核心数
        int cpuNum = Runtime.getRuntime().availableProcessors();
        //查询待处理的任务（最多取出cpuNum个任务）
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, cpuNum);
        //任务数量
        int size = mediaProcessList.size();
        log.debug("取到的视频处理任务数：" + size);
        if (size <= 0) {
            return;
        }
        //创建一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //遍历任务
        mediaProcessList.forEach(mediaProcess -> {
            //将任务加入线程池
            executorService.execute(() -> {
                try {
                    //任务id
                    long taskId = mediaProcess.getId();
                    //开启任务
                    boolean result = mediaFileProcessService.startTask(mediaProcess.getId());
                    if (!result) {
                        log.debug("抢占任务失败，任务id：{}", taskId);
                        return;
                    }
                    //执行视频转码
                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    String fileId = mediaProcess.getFileId();
                    //下载minio的视频到本地
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.debug("下载minio的视频到本地失败，任务id：{},objectName:{}", taskId, objectName);
                        //保存任务处理失败的结果
                        mediaFileProcessService.updateProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }
                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    //转换后mp4文件的名称(文件id就是Md5值)
                    String mp4_name = fileId + ".mp4";
                    //转换后mp4文件的路径
                    //先创建一个临时文件，作为转换后的文件
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (Exception e) {
                        log.debug("创建临时文件失败，{}", e.getMessage());
                        //保存任务处理失败的结果
                        mediaFileProcessService.updateProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件失败");
                        return;
                    }
                    String mp4_path = mp4File.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_path, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success，失败将返回原因
                    String res = videoUtil.generateMp4();
                    if (!res.equals("success")) {
                        log.debug("视频转码失败,原因：{},bucket:{},objectName:{}", res, bucket, objectName);
                        //保存任务处理失败的结果
                        mediaFileProcessService.updateProcessFinishStatus(taskId, "3", fileId, null, res);
                        return;
                    }
                    //上传到minio
                    boolean b = mediaFileService.addMediafilesToMinio(mp4_path, "video/mp4", bucket, objectName);
                    if (!b) {
                        log.debug("上传到minio失败，taskId:{}", taskId);
                        //保存任务处理失败的结果
                        mediaFileProcessService.updateProcessFinishStatus(taskId, "3", fileId, null, "上传到minio失败");
                        return;
                    }
                    //mp4文件的url
                    String url = getFilePath(fileId, ".mp4");
                    //更新任务状态为成功
                    mediaFileProcessService.updateProcessFinishStatus(taskId, "2", fileId, url, null);
                } finally {
                    //计算器减一(无论有无异常都会执行)
                    countDownLatch.countDown();
                }
            });
        });
        //阻塞，保证每个线程都执行完任务才结束
        //阻塞最多等待30分钟后解除
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    //根据Md5值和扩展名获得url
    private String getFilePath(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


}
