package com.yixue.content.service;

import com.yixue.content.model.dto.SaveTeachplanDto;
import com.yixue.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-07
 * @desc 课程计划管理相关接口
 */
public interface TeachplanService {

    /**
     * 根据课程id查询课程计划
     *
     * @param courseId 课程id
     * @return 课程计划列表
     */
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 新增/修改/保存课程计划
     *
     * @param saveTeachplanDto 课程计划信息
     */
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto);
}
