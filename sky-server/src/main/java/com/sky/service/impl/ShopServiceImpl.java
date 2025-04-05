package com.sky.service.impl;

import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import com.sky.constant.StatusConstant;

import java.util.Objects;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class ShopServiceImpl implements ShopService {
    @Autowired
    RedisTemplate redisTemplate;

    private static final String KEY = "shopStatus";

    @Override
    public Integer setShopStatus(Integer status) {

        redisTemplate.delete(KEY);

        ValueOperations valueOperations = redisTemplate.opsForValue();

        if(status.equals(StatusConstant.ENABLE)) {
            valueOperations.set(KEY, StatusConstant.ENABLE);

            return StatusConstant.ENABLE;
        } else if(status.equals(StatusConstant.DISABLE)) {
            valueOperations.set(KEY, StatusConstant.DISABLE);

            return StatusConstant.DISABLE;
        }


        log.info("设置的状态为{}", valueOperations.get(KEY));

        throw new RuntimeException("Illegal Input of Status");
    }

    /**
     * 获取店铺状态
     * @return
     */
    @Override
    public Integer getShopStatus() {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        return (Integer) valueOperations.get(KEY);
    }
}
