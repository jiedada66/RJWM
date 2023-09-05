package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import org.springframework.stereotype.Service;

public interface DishService extends IService<Dish> {
    void saveDishAndDishFlavor(DishDto dishDto);

    DishDto getDishAndDishFlavor(Long id);

    void updateDishAndDishFlavor(DishDto dishDto);

    void deleteDishAndDishFlavor(String ids);

    void updateStatus(Long sta, String ids);
}
