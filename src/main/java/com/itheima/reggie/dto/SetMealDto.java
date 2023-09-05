package com.itheima.reggie.dto;

import com.itheima.reggie.entity.SetMeal;
import com.itheima.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetMealDto extends SetMeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
