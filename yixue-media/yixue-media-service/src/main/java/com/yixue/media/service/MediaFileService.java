package com.yixue.media.service;

import com.yixue.base.model.PageParams;
import com.yixue.base.model.PageResult;
import com.yixue.media.model.dto.QueryMediaParamsDto;
import com.yixue.media.model.dto.UploadFileParamsDto;
import com.yixue.media.model.dto.UploadFileResultDto;
import com.yixue.media.model.entity.MediaFiles;

/**
 * @author Tavis
 * @description 媒资文件管理业务类
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @description 媒资文件查询接口
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * @param companyId           机构id
     * @param uploadFileParamsDto 上传文件的信息
     * @param localFilePath       本地文件路径
     * @return UploadFileResultDto 结果信息
     * @description 上传文件接口
     */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5
     * @param uploadFileParamsDto 上传文件的信息
     * @param bucket              桶
     * @param objectName          对象名称
     * @return MediaFiles 媒资文件信息
     * @description 将上传的文件信息保存到数据库
     */
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName);

}
