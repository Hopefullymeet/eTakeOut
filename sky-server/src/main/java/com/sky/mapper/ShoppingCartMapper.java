package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Mapper
public interface ShoppingCartMapper {

    /**
     * 查询对应的购物车内容
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> select(ShoppingCart shoppingCart);

    /**
     * 添加商品或套餐到购物车中
     * @param shoppingCart
     */
    void insertShoppingCart(ShoppingCart shoppingCart);

    /**
     * 根据id更新购物车中的数量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 根据id删除购物车
     * @param shoppingCart
     */
    @Delete("delete from shopping_cart where id = #{id} and user_id = #{userId}")
    void deleteById(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除该用户购物车中所有的商品
     * @param shoppingCart
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void clean(ShoppingCart shoppingCart);
}
