package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Mapper
public interface SetmealDishMapper {

    @Select("select * from setmeal_dish where dish_id = #{id}")
    List<SetmealDish> selectByDishId(Long id);
}
