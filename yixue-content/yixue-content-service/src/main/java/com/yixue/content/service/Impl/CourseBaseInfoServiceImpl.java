package com.yixue.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yixue.base.exception.YixueException;
import com.yixue.base.model.PageParams;
import com.yixue.base.model.PageResult;
import com.yixue.content.mapper.CourseBaseMapper;
import com.yixue.content.mapper.CourseCategoryMapper;
import com.yixue.content.mapper.CourseMarketMapper;
import com.yixue.content.model.dto.AddCourseDto;
import com.yixue.content.model.dto.CourseBaseInfoDto;
import com.yixue.content.model.dto.QueryCourseParamsDto;
import com.yixue.content.model.entity.CourseBase;
import com.yixue.content.model.entity.CourseCategory;
import com.yixue.content.model.entity.CourseMarket;
import com.yixue.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-02
 * @desc 课程基本信息管理业务实现类
 */
@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件，根据课程名称模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        //构建查询条件，根据课程审核状态精确查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());
        //构建查询条件，根据课程发布状态精确查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()), CourseBase::getStatus, queryCourseParamsDto.getPublishStatus());
        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

//        //参数合法性校验
//        if (StringUtils.isBlank(dto.getName())) {
//            YixueException.cast("课程名称为空");
//        }
//
//        if (StringUtils.isBlank(dto.getMt())) {
//            YixueException.cast("课程分类为空");
//        }
//
//        if (StringUtils.isBlank(dto.getSt())) {
//            YixueException.cast("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getGrade())) {
//            YixueException.cast("课程等级为空");
//        }
//
//        if (StringUtils.isBlank(dto.getTeachmode())) {
//            YixueException.cast("教育模式为空");
//        }
//
//        if (StringUtils.isBlank(dto.getUsers())) {
//            YixueException.cast("适应人群为空");
//        }
//
//        if (StringUtils.isBlank(dto.getCharge())) {
//            YixueException.cast("收费规则为空");
//        }
        //向课程基本信息表course_base写入数据
        CourseBase NewCourseBase = new CourseBase();
        BeanUtils.copyProperties(dto, NewCourseBase);
        NewCourseBase.setCompanyId(companyId);
        NewCourseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        NewCourseBase.setAuditStatus("202002");
        //发布状态默认为未发布
        NewCourseBase.setStatus("203001");
        //插入数据库
        int insert = courseBaseMapper.insert(NewCourseBase);
        if (insert <= 0) {
            YixueException.cast("添加课程失败");
        }
        //向课程营销表course_market写入数据
        CourseMarket NewCourseMarket = new CourseMarket();
        //将页面输入的信息拷入对象
        BeanUtils.copyProperties(dto, NewCourseMarket);
        //设置课程id
        Long courseId = NewCourseBase.getId();
        NewCourseMarket.setId(courseId);
        //保存营销信息
        saveCourseMarket(NewCourseMarket);
        //从数据库查询课程的详细信息
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }

    //查询课程信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {
        //从course_base表中查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        //从course_market表中查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //组装在一起
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        //通过courseCategoryMapper查询分类信息，将分类名称设置到courseBaseInfoDto中
        String mt = courseBase.getMt();
        CourseCategory c1 = courseCategoryMapper.selectById(mt);
        if (c1 != null) {
            courseBaseInfoDto.setMt(c1.getName());
        }
        String st = courseBase.getSt();
        CourseCategory c2 = courseCategoryMapper.selectById(st);
        if (c2 != null) {
            courseBaseInfoDto.setSt(c2.getName());
        }
        return courseBaseInfoDto;
    }


    //保存营销信息
    private void saveCourseMarket(CourseMarket NewCourseMarket) {
        //参数的合法性校验
        String charge = NewCourseMarket.getCharge();
        if (StringUtils.isEmpty(charge)) {
            YixueException.cast("收费规则为空");
        }
        //如果课程收费，价格没有填写也需要抛出异常
        if (charge.equals("201001")) {
            if (NewCourseMarket.getPrice() == null || NewCourseMarket.getPrice().floatValue() <= 0) {
                YixueException.cast("课程价格不能为空并且必须大于0");
            }
        }
        //从数据库查询营销信息，存在则更新，不存在则添加
        Long id = NewCourseMarket.getId();
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if (courseMarket == null) {
            //插入数据库
            courseMarketMapper.insert(NewCourseMarket);
        } else {
            //将NewCourseMarket中的数据拷贝到courseMarket中
            BeanUtils.copyProperties(NewCourseMarket, courseMarket);
            courseMarket.setId(NewCourseMarket.getId());
            //更新数据库
            courseMarketMapper.updateById(courseMarket);
        }
    }


}
