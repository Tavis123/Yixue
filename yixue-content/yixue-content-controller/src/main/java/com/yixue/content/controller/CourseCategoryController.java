package com.yixue.content.controller;

import com.yixue.content.model.dto.CourseCategoryTreeDto;
import com.yixue.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-02
 * @desc 课程分类查询控制器
 */
@RestController
public class CourseCategoryController {

    @Autowired
    CourseCategoryService CourseCategoryService;

    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return CourseCategoryService.queryTreeNodes("1");
    }
}
