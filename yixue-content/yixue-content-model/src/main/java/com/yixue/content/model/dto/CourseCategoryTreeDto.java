package com.yixue.content.model.dto;

import com.yixue.content.model.entity.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-02
 * @desc 课程分类树形结构模型类
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
