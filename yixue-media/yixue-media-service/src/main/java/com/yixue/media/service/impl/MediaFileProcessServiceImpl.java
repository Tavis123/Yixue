package com.yixue.media.service.impl;

import com.yixue.media.mapper.MediaProcessMapper;
import com.yixue.media.model.entity.MediaProcess;
import com.yixue.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-19
 * @desc 媒资文件处理业务实现类
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, count);
    }
}
