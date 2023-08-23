package com.yixue.ucenter.service;

import com.yixue.ucenter.model.po.User;

/**
 * @author Tavis
 * @date 2023-08-23
 * @desc 微信认证接口
 */
public interface WxAuthService {

    /**
     * 微信扫码认证，申请令牌，携带令牌查询用户信息，保存用户信息到数据库
     *
     * @param code 微信授权码
     * @return
     */
    public User wxAuth(String code);
}
