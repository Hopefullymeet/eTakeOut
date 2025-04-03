package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

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
     * @param id
     */
    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteById(Long id);

}
