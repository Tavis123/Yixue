package com.yixue.ucenter.service.Impl;

import com.alibaba.fastjson.JSON;
import com.yixue.ucenter.mapper.MenuMapper;
import com.yixue.ucenter.mapper.UserMapper;
import com.yixue.ucenter.model.dto.AuthParamsDto;
import com.yixue.ucenter.model.dto.UserExtension;
import com.yixue.ucenter.model.po.Menu;
import com.yixue.ucenter.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-21
 * @desc 用户信息查询类
 */

@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    MenuMapper menuMapper;

    //spring容器
    @Autowired
    ApplicationContext applicationContext;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //将传入的Json(字符串s)转成AuthParamsDto对象
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证参数不符合要求");
        }

        //认证类型：password,wx....
        String authType = authParamsDto.getAuthType();
        //根据认证类型从spring类型中取出指定的Bean
        String beanname = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanname, AuthService.class);
        //调用统一execute方法完成认证
        UserExtension userExtension = authService.execute(authParamsDto);
        //封装userExtension用户信息为UserDetails对象（根据此对象生成令牌）
        return getUserPrincipal(userExtension);
    }

    //封装userExtension用户信息为UserDetails对象
    public UserDetails getUserPrincipal(UserExtension user) {
        //密码
        String password = user.getPassword();
        //权限
        String[] authorities = null;
        //根据用户id查询用户的权限
        List<Menu> menus = menuMapper.selectPermissionByUserId(user.getId());
        if (menus.size() > 0) {
            List<String> permissions = new ArrayList<>();
            menus.forEach(menu -> {
                permissions.add(menu.getCode());
            });
            //将permissions转成数组
            authorities = permissions.toArray(new String[0]);
        }
        //转Json前要将敏感信息置空
        user.setPassword(null);
        //将用户信息转Json
        String userJson = JSON.toJSONString(user);
        //封装成UserDetails对象
        return User.withUsername(userJson).password(password).authorities(authorities).build();
    }

}
