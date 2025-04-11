package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理订单超时未支付
     */
    @Scheduled(cron = "0 * * * * ?")
    public void notPaid() {
        List<Orders> ordersList = orderMapper.selectSpecificStatus(Orders.PENDING_PAYMENT);

        for(Orders orders : ordersList) {
            if(orders.getOrderTime().plusMinutes(15).isBefore(LocalDateTime.now())) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时，自动取消");
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 定时检查未送达订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void notDelivered() {
        List<Orders> ordersList = orderMapper.selectSpecificStatus(Orders.DELIVERY_IN_PROGRESS);

        for(Orders orders : ordersList) {
            orders.setStatus(Orders.COMPLETED);
            orders.setDeliveryTime(LocalDateTime.now());
        }
    }
}
