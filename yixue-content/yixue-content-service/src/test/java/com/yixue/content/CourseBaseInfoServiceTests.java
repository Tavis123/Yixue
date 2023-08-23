package com.yixue.content;

import com.yixue.base.model.PageParams;
import com.yixue.base.model.PageResult;
import com.yixue.content.model.dto.QueryCourseParamsDto;
import com.yixue.content.model.entity.CourseBase;
import com.yixue.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Tavis
 * @date 2023-08-02
 * @desc 课程基本信息管理业务接口测试类
 */
@SpringBootTest
public class CourseBaseInfoServiceTests {

//    @Autowired
//    CourseBaseInfoService courseBaseInfoService;
//
//    @Test
//    public void testCourseBaseInfoService() {
//        //查询条件
//        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
//        courseParamsDto.setCourseName("java");//课程名称查询条件
//        courseParamsDto.setAuditStatus("202004");
//        //分页参数对象
//        PageParams pageParams = new PageParams();
//        pageParams.setPageNo(2L);
//        pageParams.setPageSize(2L);
//        Long companyId = 1L;
//        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(companyId, pageParams, courseParamsDto);
//        System.out.println(courseBasePageResult);
//    }
}
