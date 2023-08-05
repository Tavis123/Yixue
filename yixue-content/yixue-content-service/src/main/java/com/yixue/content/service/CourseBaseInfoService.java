package com.yixue.content.service;

import com.yixue.base.model.PageParams;
import com.yixue.base.model.PageResult;
import com.yixue.content.model.dto.AddCourseDto;
import com.yixue.content.model.dto.CourseBaseInfoDto;
import com.yixue.content.model.dto.QueryCourseParamsDto;
import com.yixue.content.model.entity.CourseBase;

/**
 * @author Tavis
 * @date 2023-08-02
 * @desc 课程基本信息管理业务接口
 */
public interface CourseBaseInfoService  {


    /*
  * @description 课程分页查询
  * @param pageParams 分页参数
  * @param queryCourseParamsDto 查询条件
  * @return 查询结果
    */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);


    /*
     * @description 新增课程
     * @param companyId 机构id
     * @param addCourseDto 新增课程信息
     * @return 课程详细信息
     */
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);


}
