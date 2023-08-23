package com.yixue.ucenter.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yixue.ucenter.mapper.UserMapper;
import com.yixue.ucenter.mapper.UserRoleMapper;
import com.yixue.ucenter.model.dto.AuthParamsDto;
import com.yixue.ucenter.model.dto.UserExtension;
import com.yixue.ucenter.model.po.User;
import com.yixue.ucenter.model.po.UserRole;
import com.yixue.ucenter.service.AuthService;
import com.yixue.ucenter.service.WxAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @author Tavis
 * @date 2023-08-22
 * @desc 微信认证实现类
 */

@Slf4j
@Service("wechat_authservice")
public class WxAuthServiceImpl implements AuthService, WxAuthService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserRoleMapper userRoleMapper;

    //用于调用第三方接口
    @Autowired
    RestTemplate restTemplate;

    @Lazy
    @Autowired
    WxAuthServiceImpl currentService;

    @Value("${weixin.appid}")
    String appid;

    @Value("${weixin.secret}")
    String secret;

    @Override
    public UserExtension execute(AuthParamsDto authParamsDto) {
        //账号
        String username = authParamsDto.getUsername();
        //根据username查询数据库
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户不存在！");
        }
        UserExtension userExtension = new UserExtension();
        BeanUtils.copyProperties(user, userExtension);
        return userExtension;
    }

    @Override
    public User wxAuth(String code) {
        //远程调用微信认证接口，申请令牌
        Map<String, String> accessTokenMap = getAccessToken(code);
        //访问令牌
        String accessToken = accessTokenMap.get("access_token");
        //用户唯一标识
        String openid = accessTokenMap.get("openid");
        //拿到令牌查询用户信息
        Map<String, String> userInfo = getUserinfo(accessToken, openid);
        //将用户信息写入数据库
        return currentService.addWxUser(userInfo);
    }

    //用授权码code申请令牌
    private Map<String, String> getAccessToken(String code) {

        String wxUrlTemplate = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
        //请求微信地址
        String wxUrl = String.format(wxUrlTemplate, appid, secret, code);
        log.info("调用微信接口申请access_token, url:{}", wxUrl);
        //发送请求
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        //获得Json数据
        String result = exchange.getBody();
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        //将Json数据转换为Map
        return JSON.parseObject(result, Map.class);
    }

    //用令牌access_token查询用户信息
    private Map<String, String> getUserinfo(String accessToken, String openid) {
        String wxUrlTemplate = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
        //请求微信地址
        String wxUrl = String.format(wxUrlTemplate, accessToken, openid);
        log.info("调用微信接口申请access_token, url:{}", wxUrl);
        //发送请求
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        //防止乱码进行转码
        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        //将Json数据转换为Map
        return JSON.parseObject(result, Map.class);
    }

    //将用户信息写入数据库
    @Transactional
    public User addWxUser(Map userInfo) {
        //获取用户唯一标识unionid
        String unionid = (String) userInfo.get("unionid");
        //昵称
        String nickname = (String) userInfo.get("nickname");
        //根据unionid查询用户信息
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getWxUnionid, unionid));
        if (user != null) {
            //如果用户存在，直接返回
            return user;
        }
        //如果用户不存在，将用户信息写入数据库
        String userId = UUID.randomUUID().toString();
        //向User表写入信息
        user = new User();
        user.setId(userId);
        user.setUsername(unionid);
        user.setNickname(nickname);
        user.setPassword(unionid);
        user.setWxUnionid(unionid);
        user.setName(nickname);
        user.setUtype("101001");//学生类型
        user.setStatus("1");//用户状态
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        //向UserRole表写入信息
        UserRole userRole = new UserRole();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setUserId(userId);
        userRole.setRoleId("17");//学生角色
        userRole.setCreateTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);

        return user;
    }

}
