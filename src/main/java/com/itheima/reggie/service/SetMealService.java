package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetMealDto;
import com.itheima.reggie.entity.SetMeal;
import org.springframework.stereotype.Service;

import java.util.List;

public interface SetMealService extends IService<SetMeal> {
    /**
     * 保存套餐以及套餐与菜品关系
     * @param setMealDto
     */
    void saveWithDish(SetMealDto setMealDto);

    /**
     * 删除套餐以及套餐与菜品关系
     * @param ids
     */
    void deleteWithDish(List<Long> ids);

    /**
     * 修改套餐状态
     * @param sta
     * @param ids
     */
    void updateStatus(int sta, List<Long> ids);

    /**
     * 根据id查询套餐以及套餐与菜品关系
     * @param id
     * @return
     */
    SetMealDto getByIdWithDish(Long id);

    /**
     * 修改套餐以及套餐与菜品关系
     * @param setMealDto
     */
    void updateWithDish(SetMealDto setMealDto);
}
