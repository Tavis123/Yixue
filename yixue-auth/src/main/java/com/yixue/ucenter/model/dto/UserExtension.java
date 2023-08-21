package com.yixue.ucenter.model.dto;

import com.yixue.ucenter.model.po.User;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tavis
 * @date 2023-08-20
 * @description 用户扩展信息
 */
@Data
public class UserExtension extends User {
    //用户权限
    List<String> permissions = new ArrayList<>();
}
