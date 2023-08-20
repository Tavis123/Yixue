package com.yixue.media.service.impl;

import com.yixue.media.mapper.MediaFilesMapper;
import com.yixue.media.mapper.MediaProcessHistoryMapper;
import com.yixue.media.mapper.MediaProcessMapper;
import com.yixue.media.model.entity.MediaFiles;
import com.yixue.media.model.entity.MediaProcess;
import com.yixue.media.model.entity.MediaProcessHistory;
import com.yixue.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-19
 * @desc 媒资文件业务实现类
 */
@Slf4j
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    MediaFilesMapper MediaFilesMapper;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    //查询待处理任务
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    //开启一个任务（抢锁）
    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result <= 0 ? false : true;
    }

    //更新任务处理状态
    @Override
    public void updateProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //查找要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return;
        }

        //如果任务执行失败
        if ("3".equals(status)) {
            //更新MediaProcess表的状态
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            return;
        }

        //如果任务执行成功
        //更新MediaFiles表的状态
        MediaFiles mediaFiles = MediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        MediaFilesMapper.updateById(mediaFiles);

        //更新MediaProcess表的状态
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);

        //将MediaProcess表的记录插入到MediaProcessHistory表中
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //从MediaProcess表中删除当前任务
        mediaProcessMapper.deleteById(taskId);

    }
}
