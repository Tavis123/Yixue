package com.yixue.media.service;

import com.yixue.base.model.PageParams;
import com.yixue.base.model.PageResult;
import com.yixue.media.model.dto.QueryMediaParamsDto;
import com.yixue.media.model.entity.MediaFiles;

/**
 * @description 媒资文件管理业务类
 * @author Tavis
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @author Tavis
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


}
