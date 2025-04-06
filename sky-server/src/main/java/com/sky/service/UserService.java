package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserLoginVO;

import java.io.IOException;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
public interface UserService {
    /**
     * 用户登录请求
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);
}
