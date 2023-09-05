package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 接收前端传来的菜品和口味数据
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors;

    private String categoryName;

    private Integer copies;
}
