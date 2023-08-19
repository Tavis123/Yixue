package com.yixue.media.service;

import com.yixue.media.model.entity.MediaProcess;

import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-19
 * @desc 媒资文件处理业务类
 */
public interface MediaFileProcessService {

    /**
     * @param shardIndex 分片下标
     * @param shardTotal 分片总数
     * @param count      查询数量
     * @description 保存媒资文件处理信息
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

}
