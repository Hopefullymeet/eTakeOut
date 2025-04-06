package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Mapper
public interface DishFlavorMapper {

    /**
     * 增加口味
     * @param dishFlavor
     */
    void insertFlavor(DishFlavor dishFlavor);

    /**
     * 根据菜品id删除套餐——菜品表中的元素
     * @param dishId
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 根据id批量删除
     * @param ids
     */
    void deleteInBatch(List<Long> ids);

    /**
     * 根据dish_id筛选dish——flavor
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> selectDishFlavors(Long dishId);

}
