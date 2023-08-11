package com.yixue.media.controller;

import com.yixue.base.model.PageParams;

import com.yixue.base.model.PageResult;
import com.yixue.media.service.MediaFileService;
import com.yixue.media.model.dto.QueryMediaParamsDto;
import com.yixue.media.model.entity.MediaFiles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 媒资文件管理接口
 * @author Tavis
 */
 @Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
 @RestController
public class MediaFilesController {

  @Autowired
  MediaFileService mediaFileService;

 @ApiOperation("媒资列表查询接口")
 @PostMapping("/files")
 public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
  Long companyId = 1232141425L;
  return mediaFileService.queryMediaFiels(companyId,pageParams,queryMediaParamsDto);

 }

}
