package com.yixue.content.controller;

import com.yixue.content.model.dto.SaveTeachplanDto;
import com.yixue.content.model.dto.TeachplanDto;
import com.yixue.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-06
 * @desc
 */
@RestController
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    //查询课程计划
    @ApiOperation("查询课程计划")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        return teachplanService.findTeachplanTree(courseId);
    }

    //创建/修改课程计划
    @ApiOperation("创建/修改课程计划")
    @PostMapping("/teachplan/save")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan) {
        teachplanService.saveTeachplan(teachplan);
    }

}

