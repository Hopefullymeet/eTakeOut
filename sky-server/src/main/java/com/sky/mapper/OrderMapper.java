package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 查询指定用户的历史订单
     * @param orders
     * @return
     */
    List<Orders> selectByUserId(Orders orders);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders selectById(Long id);

    /**
     * 条件查询
     * @param ordersPageQueryDTO
     * @return
     */
    List<Orders> conditionSelect(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Select("select status from orders")
    List<Integer> selectStatus();
}
