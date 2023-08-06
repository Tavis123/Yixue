package com.yixue.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Tavis
 * @date 2023-08-06
 * @desc 修改课程dto
 */
@Data
@ApiModel(value = "修改课程dto", description = "修改课程基本信息")
public class EditCourseDto extends AddCourseDto {

    @ApiModelProperty(value = "课程id", required = true)
    private Long id;

}
