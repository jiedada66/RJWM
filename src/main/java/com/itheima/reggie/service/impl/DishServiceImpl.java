package com.itheima.reggie.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    public void saveDishAndDishFlavor(DishDto dishDto) {
        this.save(dishDto); //保存菜品基本信息
        //保存菜品口味信息
        dishDto.getFlavors().forEach(dishFlavor -> dishFlavor.setDishId(dishDto.getId()));
        dishFlavorService.saveBatch(dishDto.getFlavors());

    }

    @Override
    public DishDto getDishAndDishFlavor(Long id) {
        //根据id查询菜品基本信息
        Dish dish = this.getById(id);
        //根据id查询菜品口味信息
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> list = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
        //将菜品基本信息和菜品口味信息封装到DishDto中
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(list);
        //返回DishDto
        return dishDto;
    }

    @Override
    public void updateDishAndDishFlavor(DishDto dishDto) {
        //更新菜品基本信息
        this.updateById(dishDto);
        //更新菜品口味信息
        //先删除原来的菜品口味信息
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
        //再保存新的菜品口味信息
        dishDto.getFlavors().forEach(dishFlavor -> dishFlavor.setDishId(dishDto.getId()));
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }

    @Override
    public void deleteDishAndDishFlavor(String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            //删除菜品基本信息
            this.removeById(id);
            //删除菜品口味信息
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,id);
            dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
        }
    }

    @Override
    public void updateStatus(Long sta, String ids) {
        String[] split = ids.split(",");
        for (String s : split) {
            Dish dish = this.getById(s);
            dish.setStatus(sta.intValue());
            this.updateById(dish);
        }
    }


}
