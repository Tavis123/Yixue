package com.yixue.content.model.dto;

import com.yixue.content.model.entity.Teachplan;
import com.yixue.content.model.entity.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-06
 * @desc 课程计划信息模型类
 */
@Data
@ToString
public class TeachplanDto extends Teachplan {
    //与媒资关联的信息
    private TeachplanMedia teachplanMedia;

    //小章节
    private List<TeachplanDto> teachPlanTreeNodes;

}
