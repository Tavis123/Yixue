package com.yixue.checkcode.model.dto;

import lombok.Data;

/**
 * @author Tavis
 * @date 2023-08-22
 * @desc 验证码生成参数类
 */

@Data
public class CheckCodeParamsDto {

    //验证码类型：pic、sms、email等
    private String checkCodeType;

    //业务携带参数
    private String param1;
    private String param2;
    private String param3;
}
