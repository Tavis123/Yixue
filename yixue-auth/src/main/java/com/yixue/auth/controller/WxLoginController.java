package com.yixue.auth.controller;

import com.yixue.ucenter.model.po.User;
import com.yixue.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * @author Tavis
 * @date 2023-08-23
 * @desc 接收微信下发的授权码接口
 */

@Slf4j
@Controller
public class WxLoginController {

    @Autowired
    WxAuthService wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}", code, state);
        //远程调用微信申请令牌，拿到令牌查询用户信息，将用户信息写入数据库
        User user = wxAuthService.wxAuth(code);
        if (user == null) {
            return "redirect:https://www.baidu.com/";
        }
        String username = user.getUsername();
        //重定向到登录界面，自动登录
        return "redirect:https://www.baidu.com/?username=" + username + "&authType=wx";
    }
}
