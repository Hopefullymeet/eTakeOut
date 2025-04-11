package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        StringBuffer dateListStr = new StringBuffer();
        StringBuffer turnoverListStr = new StringBuffer();

        List<LocalDate> dateList = new ArrayList<>();

        while(!begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        dateList.add(end);

        for(LocalDate now : dateList) {
            BigDecimal turnover = orderMapper.selectDateSale(now.atStartOfDay(), now.atStartOfDay().plusDays(1));
            dateListStr.append(now).append(",");
            turnoverListStr.append(turnover).append(",");
        }

        return TurnoverReportVO.builder()
                .dateList(dateListStr.toString())
                .turnoverList(turnoverListStr.toString())
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        StringBuffer dateListStr = new StringBuffer();
        StringBuffer newUserListStr = new StringBuffer();
        StringBuffer totalUserListStr = new StringBuffer();

        Integer totalUserNum = 0;

        List<LocalDate> dateList = new ArrayList<>();

        while(!begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        dateList.add(end);

        for(LocalDate now : dateList) {
            Integer newUserNum = userMapper.selectNewUser(now.atStartOfDay(), now.atStartOfDay().plusDays(1));
            totalUserNum += newUserNum;

            dateListStr.append(now).append(",");
            newUserListStr.append(newUserNum).append(",");
            totalUserListStr.append(totalUserNum).append(",");
        }

        return UserReportVO.builder()
                .dateList(dateListStr.toString())
                .newUserList(newUserListStr.toString())
                .totalUserList(totalUserListStr.toString())
                .build();
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        StringBuffer dateListStr = new StringBuffer();
        StringBuffer orderCountListStr = new StringBuffer();
        StringBuffer validOrderCountListStr = new StringBuffer();

        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;

        List<LocalDate> dateList = new ArrayList<>();

        while(!begin.equals(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        dateList.add(end);

        for(LocalDate now : dateList) {
            Integer totalOrderByDay = orderMapper.orderCount(now.atStartOfDay(), now.atStartOfDay().plusDays(1));
            Integer validOrderByDay = orderMapper.validOrderCount(now.atStartOfDay(), now.atStartOfDay().plusDays(1));

            totalOrderCount += totalOrderByDay;
            validOrderCount += validOrderByDay;

            dateListStr.append(now).append(",");
            orderCountListStr.append(totalOrderByDay).append(",");
            validOrderCountListStr.append(validOrderByDay).append(",");
        }

        Double orderCompletionRate = validOrderCount * 1.0 / totalOrderCount;


        return OrderReportVO.builder()
                .dateList(dateListStr.toString())
                .orderCountList(orderCountListStr.toString())
                .validOrderCountList(validOrderCountListStr.toString())
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .build();
    }

    /**
     * 查询销量排名top10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        StringBuffer nameListStr = new StringBuffer();
        StringBuffer numberListStr = new StringBuffer();

        List<String> nameList = orderMapper.selectTop10Names(begin.atStartOfDay(), end.atStartOfDay().plusDays(1));
        List<Integer> numberList = orderMapper.selectTop10Numbers(begin.atStartOfDay(), end.atStartOfDay().plusDays(1));

        for(int i = 0; i < 10; i++) {
            if(i >= nameList.size() - 1) {
                break;
            }

            nameListStr.append(nameList.get(i)).append(",");
            numberListStr.append(numberList.get(i)).append(",");
        }

        return SalesTop10ReportVO.builder()
                .nameList(nameListStr.toString())
                .numberList(numberListStr.toString())
                .build();
    }
}
