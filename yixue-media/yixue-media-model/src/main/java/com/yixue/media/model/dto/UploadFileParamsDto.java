package com.yixue.media.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author Tavis
 * @date 2023-08-13
 * @desc 上传文件的信息（存进数据库）
 */
@Data
@ToString
public class UploadFileParamsDto {
    /**
     * 文件名称
     */
    private String filename;

    /**
     * 文件类型（文档，音频，视频）
     */
    private String fileType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 标签
     */
    private String tags;

    /**
     * 上传人
     */
    private String username;

    /**
     * 备注
     */
    private String remark;

}
