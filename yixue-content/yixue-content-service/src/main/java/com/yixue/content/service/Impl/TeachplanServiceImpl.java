package com.yixue.content.service.Impl;

import com.yixue.content.mapper.TeachplanMapper;
import com.yixue.content.model.dto.TeachplanDto;
import com.yixue.content.service.TeachplanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-07
 * @desc 课程计划管理相关接口实现类
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }
}
