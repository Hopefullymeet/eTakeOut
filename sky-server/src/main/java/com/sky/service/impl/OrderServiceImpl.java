package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.utils.WeChatPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {

        // 处理异常情况（地址簿为空、购物车为空）
        AddressBook tempAddressBook = AddressBook.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<AddressBook> addressBookList = addressBookMapper.list(tempAddressBook);

        if(addressBookList.isEmpty()) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart tempShoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.select(tempShoppingCart);

        if(shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        // 向orders表中添加1条数据
        Orders orders = new Orders();
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());

        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        // 设置orders number订单号
        orders.setNumber(String.valueOf(System.currentTimeMillis()));

        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setUserName(userMapper.selectById(BaseContext.getCurrentId()).getName());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);

        // 向order_detail表中添加n条数据
        for(ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .name(shoppingCart.getName())
                    .orderId(orders.getId())
                    .number(shoppingCart.getNumber())
                    .amount(shoppingCart.getAmount())
                    .image(shoppingCart.getImage())
                    .build();

            if(shoppingCart.getDishId() != null) {
                orderDetail.setDishId(shoppingCart.getDishId());
                orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            } else if(shoppingCart.getSetmealId() != null) {
                orderDetail.setSetmealId(shoppingCart.getSetmealId());
            }

            orderDetailMapper.insert(orderDetail);
        }

        // 清空购物车
        shoppingCartMapper.clean(tempShoppingCart);

        // 封装结果返回对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }
}
