package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect{
    @Pointcut("@annotation(com.sky.annotation.AutoFill)")
    private void pt() {};

    @Before("pt()")
    public void AutoFill(JoinPoint joinPoint) {
        log.info("进入切面，开始注入");

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();   //获取方法签名
        OperationType operationType = methodSignature.getMethod().getAnnotation(AutoFill.class).value();   //获取方法的注解值

        Object[] args = joinPoint.getArgs();    //获得方法参数值（即为Employee或Category对象）

        //获取固定注入值
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();

        if(operationType == OperationType.INSERT) {
            try {
                Method setCreateTime = args[0].getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = args[0].getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = args[0].getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = args[0].getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(args[0], now);
                setCreateUser.invoke(args[0], id);
                setUpdateTime.invoke(args[0], now);
                setUpdateUser.invoke(args[0], id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = args[0].getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = args[0].getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(args[0], now);
                setUpdateUser.invoke(args[0], id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
