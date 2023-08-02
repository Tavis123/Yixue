package com.yixue.content.controller;

import com.yixue.base.model.PageParams;
import com.yixue.base.model.PageResult;
import com.yixue.content.model.dto.QueryCourseParamsDto;
import com.yixue.content.model.entity.CourseBase;
import com.yixue.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Tavis
 * @date 2023-07-31
 * @desc 课程信息编辑接口
 */
@Api(value = "课程信息编辑接口", tags = "课程信息编辑接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    //pageParams分页参数通过url的key/value传入，queryCourseParams通过json数据传入
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {

        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);

        return pageResult;
    }
}
