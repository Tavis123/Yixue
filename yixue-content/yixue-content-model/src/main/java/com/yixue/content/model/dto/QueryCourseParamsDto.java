package com.yixue.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author Tavis
 * @date 2023-07-31
 * @desc 课程查询参数dto
 */
@Data
@ToString
public class QueryCourseParamsDto {

    //审核状态
    private String auditStatus;

    //课程名称
    private String courseName;

    //发布状态
    private String publishStatus;
}
