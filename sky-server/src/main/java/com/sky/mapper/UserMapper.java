package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Mapper
public interface UserMapper {

    /**
     * 根据Openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User selectByOpenid(String openid);

    /**
     * 根据id查找用户
     * @param id
     * @return
     */
    @Select("select * from user where id = #{id}")
    User selectById(Long id);

    /**
     * 插入用户信息
     * @param user
     * @return
     */
    long insert(User user);
}
