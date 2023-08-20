package com.yixue.media.service;

import com.yixue.media.model.entity.MediaProcess;

import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-19
 * @desc 媒资文件业务类
 */
public interface MediaFileProcessService {

    /**
     * @param shardIndex 分片下标
     * @param shardTotal 分片总数
     * @param count      查询数量
     * @description 查询待处理任务
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     * @param id 任务id
     * @description 开启一个任务（抢锁）
     */
    boolean startTask(long id);

    /**
     * @param taskId 任务id
     * @param status 任务状态
     * @param fileId 文件id
     * @param url    文件url
     * @param errorMsg 错误信息
     * @description 更新任务处理状态
     */
    void updateProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
