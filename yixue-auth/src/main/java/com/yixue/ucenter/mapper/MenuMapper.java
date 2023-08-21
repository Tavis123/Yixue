package com.yixue.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yixue.ucenter.model.po.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
    @Select("SELECT	* FROM menu WHERE id IN (SELECT menu_id FROM permission WHERE role_id IN ( SELECT role_id FROM user_role WHERE user_id = #{userId} ))")
    List<Menu> selectPermissionByUserId(@Param("userId") String userId);
}
