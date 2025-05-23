package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("添加菜品接口")
    @CacheEvict(cacheNames = "setmealCache", key = "#dishDTO.categoryId")
    public Result addDish(@RequestBody DishDTO dishDTO) {
        dishService.addDish(dishDTO);

        cleanCache("dish_" + dishDTO.getCategoryId());

        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询接口")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {

        PageResult pageResult = dishService.page(dishPageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品接口")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result delete(@RequestParam List<Long> ids) {
        dishService.delete(ids);

        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 修改指定id菜品的状态
     * @param id
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("修改菜品状态接口")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result editStatus(Long id, @PathVariable Integer status) {
        dishService.editStatus(id, status);

        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品接口")
    public Result<DishVO> queryById(@PathVariable Long id) {

        DishVO dishVO = dishService.queryById(id);

        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品接口")
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result editDish(@RequestBody DishDTO dishDTO) {
        dishService.editDish(dishDTO);

//        cleanCache("dish_*");

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("分类id查询菜品接口")
    public Result<List<Dish>> queryByCategoryId(Long categoryId) {
        List<Dish> dish = dishService.queryByCategoryId(categoryId);

        return Result.success(dish);
    }

    /**
     * 用于清理缓存的方法
     * @param pattern
     */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
