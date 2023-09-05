package com.itheima.reggie.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.SetMealDto;
import com.itheima.reggie.entity.SetMeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetMealMapper;
import com.itheima.reggie.result.CustomException;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, SetMeal> implements SetMealService {

    @Autowired
    private SetMealDishService setMealDishService;

    @Override
    public void saveWithDish(SetMealDto setMealDto) {
        this.save(setMealDto);

        List<SetmealDish> setMealDishes = setMealDto.getSetmealDishes();
        for(SetmealDish setMealDish : setMealDishes) {
            setMealDish.setSetmealId(setMealDto.getId());
            setMealDishService.save(setMealDish);
        }
    }

    @Override
    public void deleteWithDish(List<Long> ids) {
        //判断套餐是否是销售状态，如果是，不能删除
        LambdaQueryWrapper<SetMeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetMeal::getId,ids).eq(SetMeal::getStatus,1);
        int count = this.count(lambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("套餐是销售状态，不能删除");
        }
        //删除套餐
        this.removeByIds(ids);
        //删除套餐与菜品关系
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        lambdaQueryWrapper1.in(SetmealDish::getSetmealId,ids);
        setMealDishService.remove(lambdaQueryWrapper1);
    }

    @Override
    public void updateStatus(int sta, List<Long> ids) {
        for (Long id : ids) {
            SetMeal setMeal = this.getById(id);
            setMeal.setStatus(sta);
            this.updateById(setMeal);
        }
    }

    @Override
    public SetMealDto getByIdWithDish(Long id) {
        //根据id查询套餐
        SetMeal setMeal = this.getById(id);
        //根据id查询套餐与菜品关系
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setMealDishService.list(lambdaQueryWrapper);
        //将套餐与菜品关系封装到dto中
        SetMealDto setMealDto = new SetMealDto();
        BeanUtils.copyProperties(setMeal,setMealDto);
        setMealDto.setSetmealDishes(list);
        //返回dto
        return setMealDto;
    }

    @Override
    public void updateWithDish(SetMealDto setMealDto) {
        //1.修改套餐基本信息
        this.updateById(setMealDto);
        //2.修改套餐关联菜品信息
        //2.1删除套餐关联菜品信息
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId,setMealDto.getId());
        setMealDishService.remove(lambdaQueryWrapper);
        //2.2添加套餐关联菜品信息
        setMealDto.getSetmealDishes().forEach(setmealDish -> setmealDish.setSetmealId(setMealDto.getId()));
        setMealDishService.saveBatch(setMealDto.getSetmealDishes());
    }
}
