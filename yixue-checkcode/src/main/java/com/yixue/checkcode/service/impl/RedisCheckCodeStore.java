package com.yixue.checkcode.service.impl;

import com.yixue.checkcode.service.CheckCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author Tavis
 * @date 2023-08-22
 * @desc 使用redis存储验证码，测试用
 */

@Component("RedisCheckCodeStore")
public class RedisCheckCodeStore implements CheckCodeService.CheckCodeStore {

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void set(String key, String value, Integer expire) {
        redisTemplate.opsForValue().set(key,value,expire, TimeUnit.SECONDS);
    }

    @Override
    public String get(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void remove(String key) {
        redisTemplate.delete(key);
    }
}
