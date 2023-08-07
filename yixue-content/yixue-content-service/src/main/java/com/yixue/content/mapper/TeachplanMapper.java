package com.yixue.content.mapper;

import com.yixue.content.model.dto.TeachplanDto;
import com.yixue.content.model.entity.Teachplan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 课程计划 Mapper 接口
 * </p>
 *
 * @author Tavis
 */
@Mapper
public interface TeachplanMapper extends BaseMapper<Teachplan> {

    //查询课程计划
    public List<TeachplanDto> selectTreeNodes(Long courseId);
}
