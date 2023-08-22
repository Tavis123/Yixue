package com.yixue.ucenter.service;

import com.yixue.ucenter.model.dto.AuthParamsDto;
import com.yixue.ucenter.model.dto.UserExtension;

/**
 * @author Tavis
 * @date 2023-08-22
 * @desc 统一认证接口
 */
public interface AuthService {

    /**
     * @param authParamsDto 认证参数
     * @return UserExtension 用户信息
     */
    UserExtension execute(AuthParamsDto authParamsDto);

}
