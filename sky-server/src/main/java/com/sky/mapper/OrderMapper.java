package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Mapper
public interface OrderMapper {

    // TODO 设置订单id为GeneratedKey并返回
    void insert(Orders orders);
}
