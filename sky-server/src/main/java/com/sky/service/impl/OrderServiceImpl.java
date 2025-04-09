package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult queryHistoryOrders(Integer page, Integer pageSize, Integer status) {
        Orders orders = Orders.builder()
                        .userId(BaseContext.getCurrentId())
                        .status(status)
                        .build();

        // 分页操作
        PageHelper.startPage(page, pageSize);

        List<Orders> ordersList = orderMapper.selectByUserId(orders);
        Page<Orders> p = (Page<Orders>) ordersList;

        List<OrderVO> orderVOList = new ArrayList<>();
        for(Orders order : ordersList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(orderDetail);

            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setOrderDetailList(orderDetailList);

            orderVOList.add(orderVO);
        }

        PageResult pageResult = new PageResult();
        pageResult.setRecords(orderVOList);
        pageResult.setTotal(p.getTotal());

        return pageResult;
    }

    /**
     * 查询订单详细信息
     * @param id
     * @return
     */
    @Override
    public OrderVO queryOrderDetail(Long id) {
        OrderVO orderVO = new OrderVO();

        Orders orders = orderMapper.selectById(id);
        BeanUtils.copyProperties(orders, orderVO);

        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(OrderDetail.builder().orderId(id).build());
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 取消订单
     * @param id
     */
    @Override
    public void cancel(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        Orders orders = orderMapper.selectById(id);

        orders.setOrderTime(LocalDateTime.now());
        orders.setId(null);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);

        orderMapper.insert(orders);

        //插入新的order_detail表
        List<OrderDetail> orderDetailList = orderDetailMapper.selectByOrderId(OrderDetail.builder().orderId(id).build());

        for(OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orders.getId());
            orderDetailMapper.insert(orderDetail);
        }
    }

    /**
     * 条件查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        List<Orders> ordersList = orderMapper.conditionSelect(ordersPageQueryDTO);
        Page<Orders> p = (Page<Orders>) ordersList;

        List<OrderVO> orderVOList = new ArrayList<>();

        for(Orders orders : ordersList) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orders.getId());
            orderVO.setOrderDetailList(orderDetailMapper.selectByOrderId(orderDetail));

            orderVO.setOrderDishes(getOrderDishStr(orderDetail));

            orderVOList.add(orderVO);
        }

        return new PageResult(p.getTotal(), orderVOList);
    }

    /**
     * 获取相应订单的菜品字符串
     * @param orderDetail
     * @return
     */
    private String getOrderDishStr(OrderDetail orderDetail) {

        List<OrderDetail> list = orderDetailMapper.selectByOrderId(orderDetail);

        StringBuffer sb = new StringBuffer();

        for(OrderDetail orderDetail1 : list) {
            sb.append(orderDetail1.getName()).append("*").append(orderDetail1.getNumber()).append(";");
        }

        return sb.toString();
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(0);
        orderStatisticsVO.setToBeConfirmed(0);
        orderStatisticsVO.setDeliveryInProgress(0);

        List<Integer> integerList = orderMapper.selectStatus();

        for(Integer i : integerList) {
            if(i.equals(Orders.TO_BE_CONFIRMED)) {
                orderStatisticsVO.setToBeConfirmed(orderStatisticsVO.getToBeConfirmed() + 1);
            } else if(i.equals(Orders.DELIVERY_IN_PROGRESS)) {
                orderStatisticsVO.setDeliveryInProgress(orderStatisticsVO.getDeliveryInProgress() + 1);
            } else if(i.equals(Orders.CONFIRMED)) {
                orderStatisticsVO.setConfirmed(orderStatisticsVO.getConfirmed() + 1);
            }
        }

        return orderStatisticsVO;
    }

    /**
     * 接单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        log.info("order id = {}", ordersConfirmDTO.getId());
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        if(orderMapper.selectById(ordersRejectionDTO.getId()) == null || !orderMapper.selectById(ordersRejectionDTO.getId()).getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancelByAdmin(OrdersCancelDTO ordersCancelDTO) {

        Orders orders = Orders.builder()
                .cancelTime(LocalDateTime.now())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .id(ordersCancelDTO.getId())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        if(!orderMapper.selectById(id).getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        if(!orderMapper.selectById(id).getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }
}
