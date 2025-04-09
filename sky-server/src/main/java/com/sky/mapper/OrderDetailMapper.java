package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 根据OrderId查询
     * @param orderDetail
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> selectByOrderId (OrderDetail orderDetail);
}
