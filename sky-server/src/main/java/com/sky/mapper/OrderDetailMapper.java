package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Mapper
public interface OrderDetailMapper {

    /**
     * 向order_detail表中插入数据
     * @param orderDetail
     */
    void insert(OrderDetail orderDetail);
}
