package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

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

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Select("select count(*) from user where create_time between #{begin} and #{end}")
    Integer selectNewUser(LocalDateTime begin, LocalDateTime end);
}
