package com.itheima.reggie.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.result.R;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("dishDto:{}",dishDto);
        dishService.saveDishAndDishFlavor(dishDto); //新增菜品信息
        return R.success("新增菜品成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("分页查询");
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> pageInfoDto = new Page<>();
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name)
                .orderByDesc(Dish::getUpdateTime);

        dishService.page(pageInfo,lambdaQueryWrapper);
        //将pageInfo对象拷贝给pageInfoDto,但是不拷贝records属性
        BeanUtils.copyProperties(pageInfo,pageInfoDto,"records");
        //设置pageInfoDto的records属性
        List<DishDto> lists = new ArrayList<>();
        pageInfo.getRecords().forEach(dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish,dishDto);
            Category category = categoryService.getById(dish.getCategoryId());
            if (category == null) {
                dishDto.setCategoryName("未知");
            } else {
                dishDto.setCategoryName(category.getName());
            }
            lists.add(dishDto);
        });
        pageInfoDto.setRecords(lists);
        log.info("pageInfoDto:{}",pageInfoDto);
        return R.success(pageInfoDto);
    }

    /**
     * 根据id查询菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getDishAndDishFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updateDishAndDishFlavor(@RequestBody DishDto dishDto) {
        dishService.updateDishAndDishFlavor(dishDto);
        //清除对应菜品的缓存信息
        redisTemplate.delete("dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus());
        return R.success("修改菜品成功");
    }

    /**
     * 删除菜品信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(String ids) {
        dishService.deleteDishAndDishFlavor(ids);
        return R.success("删除菜品成功");
    }

    /**
     * 修改菜品状态
     * @param sta
     * @param ids
     * @return
     */
    @PostMapping("/status/{sta}")
    public R<String> updateStatus(@PathVariable Long sta, String ids) {
        dishService.updateStatus(sta,ids);
        return R.success("修改菜品状态成功");
    }

    /**
     * 根据菜品分类id查询所有菜品信息,并且查询菜品口味信息
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();

        //从redis中获取菜品缓存信息
        List<DishDto> RedisLists = (List<DishDto>) redisTemplate.opsForValue().get(key);

        //如果redis中有菜品缓存信息,则直接返回
        if (RedisLists != null) {
            return R.success(RedisLists);
        }

        //如果redis中没有菜品缓存信息,则从数据库中查询
        //根据菜品分类id查询菜品基本信息
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(dish.getName()),Dish::getName,dish.getName());
        lambdaQueryWrapper.eq(Dish::getStatus,1); //查询状态为1的菜品
        lambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(lambdaQueryWrapper);
        //查询菜品口味信息,并且封装到DishDto对象中
        List<DishDto> DtoList = new ArrayList<>();
        list.forEach(D -> DtoList.add(dishService.getDishAndDishFlavor(D.getId())));

        //将菜品缓存信息存入redis中,并且设置过期时间为60分钟
        redisTemplate.opsForValue().set(key,DtoList,60, TimeUnit.MINUTES);

        return R.success(DtoList);
    }

}
