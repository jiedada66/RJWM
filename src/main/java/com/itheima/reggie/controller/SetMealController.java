package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetMealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.SetMeal;
import com.itheima.reggie.result.R;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetMealDishService setMealDishService;

    @Autowired
    private SetMealService setMealService;

    /**
     * 新增套餐
     * @param setMealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清空setmealCache所有缓存
    public R<String> save(@RequestBody SetMealDto setMealDto) {
        log.info("setMealDto:{}",setMealDto);
        setMealService.saveWithDish(setMealDto);
        return R.success("新增成功");
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
        Page<SetMeal> pageInfo = new Page<>(page,pageSize);
        Page<SetMealDto> pageInfoDto = new Page<>();

        LambdaQueryWrapper<SetMeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),SetMeal::getName,name)
                        .orderByDesc(SetMeal::getCreateTime);
        setMealService.page(pageInfo,lambdaQueryWrapper);

        BeanUtils.copyProperties(pageInfo,pageInfoDto,"records");

        List<SetMealDto> lists = new ArrayList<>();
        pageInfo.getRecords().forEach(setMeal -> {
            SetMealDto setMealDto = new SetMealDto();
            BeanUtils.copyProperties(setMeal,setMealDto);
            Category category = categoryService.getById(setMeal.getCategoryId());
            if (category == null) {
                setMealDto.setCategoryName("未知");
            } else {
                setMealDto.setCategoryName(category.getName());
            }
            lists.add(setMealDto);
        });
        pageInfoDto.setRecords(lists);

        return R.success(pageInfoDto);
    }

    /**
     * 注意：当请求参数接收不到时，需要在参数前加上@RequestParam注解，让它强制接收
     *      也可以用String直接接收ids
     *
     * 根据ids删除套餐以及套餐中的菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清空setmealCache所有缓存
    public R<String> deleteWithDish(@RequestParam List<Long> ids) {
        log.info("ids:{}",ids);
        setMealService.deleteWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 修改套餐状态
     * @param sta
     * @param ids
     * @return
     */
    @PostMapping("/status/{sta}")
    public R<String> updateStatus(@PathVariable int sta, @RequestParam List<Long> ids) {
        log.info("sta:{},ids:{}",sta,ids);
        setMealService.updateStatus(sta,ids);
        return R.success("修改成功");
    }

    /**
     * 根据id查询套餐以及套餐中的菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetMealDto> getById(@PathVariable Long id) {
        log.info("id:{}",id);
        SetMealDto setMealDto = setMealService.getByIdWithDish(id);
        return R.success(setMealDto);
    }

    /**
     * 修改套餐以及套餐中的菜品
     * @param setMealDto
     * @return
     */
    @PutMapping
    public R<String> updateWithDish(@RequestBody SetMealDto setMealDto) {
        log.info("setMealDto:{}",setMealDto);
        setMealService.updateWithDish(setMealDto);
        return R.success("修改成功");
    }

    /**
     * 根据分类id和状态查询套餐
     * @param categoryId
     * @param status
     * unless:如果返回值为null，则不缓存
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#categoryId + '_' + #status",unless = "#result == null")
    public R<List<SetMeal>> list(@RequestParam Long categoryId,@RequestParam Long status) {
        LambdaQueryWrapper<SetMeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(categoryId != null,SetMeal::getCategoryId,categoryId)
                        .eq(status != null,SetMeal::getStatus,status)
                        .orderByDesc(SetMeal::getCreateTime);
        List<SetMeal> list = setMealService.list(lambdaQueryWrapper);
        return R.success(list);
    }

}
