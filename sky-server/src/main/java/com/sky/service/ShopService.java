package com.sky.service;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
public interface ShopService {
    /**
     * 设置店铺状态
     * @param status
     * @return
     */
    Integer setShopStatus(Integer status);

    /**
     * 获取店铺营业状态
     * @return
     */
    Integer getShopStatus();
}
