package com.yixue.content.service;

import com.yixue.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-03
 * @desc 课程分类树形结构查询
 */
public interface CourseCategoryService {

    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
