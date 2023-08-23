package com.yixue.content.controller;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.yixue.base.exception.ValidationGroups;
import com.yixue.base.model.PageParams;
import com.yixue.base.model.PageResult;
import com.yixue.content.model.dto.AddCourseDto;
import com.yixue.content.model.dto.CourseBaseInfoDto;
import com.yixue.content.model.dto.EditCourseDto;
import com.yixue.content.model.dto.QueryCourseParamsDto;
import com.yixue.content.model.entity.CourseBase;
import com.yixue.content.service.CourseBaseInfoService;
import com.yixue.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation("课程分页查询接口")
    @PreAuthorize("hasAuthority('teachmanager_course_list')")//指定权限标识符
    @PostMapping("/course/search")
    //pageParams分页参数通过url的key/value传入，queryCourseParams通过json数据传入
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {

        //当前登录用户
        SecurityUtil.User user = SecurityUtil.getUser();
        Long companyId = null;
        if (StringUtils.isNotEmpty(user.getCompanyId())) {
            Long.parseLong(user.getCompanyId());
        }
        PageResult<CourseBase> pageResult = courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParams);

        return pageResult;
    }

    @ApiOperation("新增课程接口")
    @PostMapping("/course/create")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseDto addCourseDto) {
        //获取用户所属机构的id
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }

    @ApiOperation("根据课程id查询接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable Long courseId) {
        //取出当前用户身份
        SecurityUtil.User user = SecurityUtil.getUser();
//        System.out.println(user.getUsername());
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("修改课程接口")
    @PutMapping("/course/update")
    public CourseBaseInfoDto updateCourseBase(@RequestBody @Validated EditCourseDto editCourseDto) {
        //获取用户所属机构的id
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
    }

}
