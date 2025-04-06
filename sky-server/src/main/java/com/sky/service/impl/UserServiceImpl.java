package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    WeChatProperties weChatProperties;

    @Autowired
    UserMapper userMapper;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User login(UserLoginDTO userLoginDTO){
        Map<String, String> map = new HashMap<>();

        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");

        String json = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session", map);
        String openid = JSONObject.parseObject(json).getString("openid");

        if(openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        User user = userMapper.selectByOpenid(openid);

        if(user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setCreateTime(LocalDateTime.now());

            long id = userMapper.insert(user);
            user.setId(id);
        }

        return user;
    }
}
