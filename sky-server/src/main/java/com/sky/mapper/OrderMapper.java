package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * 获取指定状态的订单
     * @return
     */
    @Select("select * from orders where status = #{status}")
    List<Orders> selectSpecificStatus(Integer status);

    /**
     * 查询当天销售额
     * @param begin
     * @param end
     * @return
     */
    @Select("select sum(amount) from orders where order_time between #{begin} and #{end} and status = 5")
    BigDecimal selectDateSale(LocalDateTime begin, LocalDateTime end);

    /**
     * 一天内订单统计
     * @param begin
     * @param end
     * @return
     */
    @Select("select count(*) from orders where order_time between #{begin} and #{end}")
    Integer orderCount(LocalDateTime begin, LocalDateTime end);

    /**
     * 一天内有效订单统计
     * @param begin
     * @param end
     * @return
     */
    @Select("select count(*) from orders where order_time between #{begin} and #{end} and status = 5")
    Integer validOrderCount(LocalDateTime begin, LocalDateTime end);

    /**
     * 筛选前十名的菜品名称
     * @param begin
     * @param end
     * @return
     */
    @Select("select name from orders right join order_detail on orders.id = order_detail.order_id where order_time between #{begin} and #{end} group by name order by sum(order_detail.number) DESC")
    List<String> selectTop10Names(LocalDateTime begin, LocalDateTime end);

    /**
     * 获取前十名的菜品销量
     * @param begin
     * @param end
     * @return
     */
    @Select("select sum(order_detail.number) from orders right join order_detail on orders.id = order_detail.order_id where order_time between #{begin} and #{end} group by name order by sum(order_detail.number) DESC")
    List<Integer> selectTop10Numbers(LocalDateTime begin, LocalDateTime end);
}
