package com.yixue.ucenter.service.Impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yixue.ucenter.feignclient.CheckCodeClient;
import com.yixue.ucenter.mapper.UserMapper;
import com.yixue.ucenter.model.dto.AuthParamsDto;
import com.yixue.ucenter.model.dto.UserExtension;
import com.yixue.ucenter.model.po.User;
import com.yixue.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Tavis
 * @date 2023-08-22
 * @desc 账号密码认证实现类
 */

@Slf4j
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;

    @Override
    public UserExtension execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();
        //验证码
        String checkcode = authParamsDto.getCheckcode();
        //验证码对应的key
        String checkcodeKey = authParamsDto.getCheckcodekey();
        if (StringUtils.isEmpty(checkcode) || StringUtils.isEmpty(checkcodeKey)) {
            throw new RuntimeException("请输入验证码！");
        }
        //远程调用验证码服务，校验验证码是否正确
        Boolean verify = checkCodeClient.verify(checkcodeKey, checkcode);
        if (verify == null || !verify) {
            throw new RuntimeException("验证码错误！");
        }
        //根据username账号查询数据库
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        //查询到用户不存在，要返回null即可，SpringSecurity框架抛出异常用户不存在
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        //正确的密码(经过BCryptPasswordEncoder加密)
        String password = user.getPassword();
        //用户输入的密码
        String inputPassword = authParamsDto.getPassword();
        //校验账号密码
        boolean matches = passwordEncoder.matches(inputPassword, password);
        if (!matches) {
            throw new RuntimeException("账号或密码错误！");
        }
        //校验通过，返回用户信息
        UserExtension userExtension = new UserExtension();
        BeanUtils.copyProperties(user, userExtension);
        return userExtension;
    }


}
