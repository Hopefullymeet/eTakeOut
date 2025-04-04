package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
public interface DishService {
    /**
     * 添加菜品
     * @param dishDTO
     */
    void addDish(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void delete(List<Long> ids);

    /**
     * 修改指定id菜品的状态
     * @param id
     * @param status
     */
    void editStatus(Long id, Integer status);

    /**
     * 根据id查询菜品信息
     * @param id
     * @return
     */
    DishVO queryById(Long id);

    /**
     * 修改菜品
     * @param dishDTO
     */
    void editDish(DishDTO dishDTO);

    /**
     * 分类id查询
     * @param categoryId
     * @return
     */
    List<Dish> queryByCategoryId(Long categoryId);
}
