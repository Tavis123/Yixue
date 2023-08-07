package com.yixue.content;

import com.yixue.content.mapper.TeachplanMapper;
import com.yixue.content.model.dto.TeachplanDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-06
 * @desc 课程计划mapper测试
 */
@SpringBootTest
public class TeachplanMapperTests {
    @Autowired
    TeachplanMapper teachplanMapper;

    @Test
    public void testSelectTreeNodes() {
        List<TeachplanDto> teachplanDto = teachplanMapper.selectTreeNodes(117L);
        System.out.println(teachplanDto);
    }
}
