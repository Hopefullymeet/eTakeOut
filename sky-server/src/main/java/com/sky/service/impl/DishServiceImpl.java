package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lyrics61
 * @version 1.0
 * @since 2025
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 添加菜品
     * @param dishDTO
     */
    @Override
    @Transactional
    public void addDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        if(dish.getStatus() == null) {
            dish.setStatus(0);
        }

        //添加一条菜品
        dishMapper.insert(dish);

        Long id = dish.getId();

        //添加所有口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        for(DishFlavor flavor : flavors) {
            flavor.setDishId(id);
            dishFlavorMapper.insertFlavor(flavor);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        List<DishVO> dishVOS = dishMapper.select(dishPageQueryDTO);
        Page<DishVO> page = (Page<DishVO>) dishVOS;

        return new PageResult(page.getTotal(), dishVOS);
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        for(Long id : ids) {
            if(dishMapper.selectById(id).getStatus() == 1) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
            if(!setmealDishMapper.selectByDishId(id).isEmpty()) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        }

        dishMapper.deleteInBatch(ids);
        dishFlavorMapper.deleteInBatch(ids);

//        for(Long id : ids) {
//            dishMapper.deleteById(id);
//            dishFlavorMapper.deleteById(id);
//        }

    }

    /**
     * 修改指定id菜品的状态
     *
     * @param id
     * @param status
     */
    @Override
    public void editStatus(Long id, Integer status) {
        dishMapper.updateStatus(id, status);
    }

    /**
     * 根据id查询菜品信息
     * @param id
     * @return
     */
    @Override
    public DishVO queryById(Long id) {
        DishVO dishVO = new DishVO();
        Dish dish = dishMapper.selectById(id);
        BeanUtils.copyProperties(dish, dishVO);

        dishVO.setCategoryName(categoryMapper.selectById(dishVO.getCategoryId()).getName());
        dishVO.setFlavors(dishFlavorMapper.selectDishFlavors(id));

        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void editDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.updateDish(dish);

        //删除口味表中的原有的所有元素
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //添加所有口味元素
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        for(DishFlavor dishFlavor : dishFlavors) {
            dishFlavorMapper.insertFlavor(dishFlavor);
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> queryByCategoryId(Long categoryId) {
        return dishMapper.selectByCategoryId(categoryId);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.selectDishFlavors(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
