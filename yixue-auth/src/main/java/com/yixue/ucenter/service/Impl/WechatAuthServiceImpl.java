package com.yixue.ucenter.service.Impl;

import com.yixue.ucenter.model.dto.AuthParamsDto;
import com.yixue.ucenter.model.dto.UserExtension;
import com.yixue.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Tavis
 * @date 2023-08-22
 * @desc 微信认证实现类
 */

@Slf4j
@Service("wechat_authservice")
public class WechatAuthServiceImpl implements AuthService {

    @Override
    public UserExtension execute(AuthParamsDto authParamsDto) {
        return null;
    }
}
