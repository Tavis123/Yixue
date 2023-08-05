package com.yixue.content.model.dto;

import com.yixue.content.model.entity.CourseBase;
import lombok.Data;

/**
 * @author Tavis
 * @date 2023-08-03
 * @desc 课程基本信息dto
 */
@Data
public class CourseBaseInfoDto extends CourseBase {
    //收费规则，对应数据字典
    private String charge;

    //价格
    private Float price;


    //原价
    private Float originalPrice;

    //咨询qq
    private String qq;

    //微信
    private String wechat;

    //电话
    private String phone;

    //有效期天数
    private Integer validDays;

    //大分类名称
    private String mtName;

    //小分类名称
    private String stName;

}
