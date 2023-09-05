package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.result.R;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("category:{}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> Page(int page,int pageSize) {
        log.info("分页查询");
        //分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        //分页查询
        categoryService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping()
    public R<String> delete(Long id) {
        log.info("删除分类，id为：{}",id);

        //categoryService.removeById(id);
        categoryService.remove(id);

        return R.success("删除分类成功");
    }

    /**
     * 根据id修改分类
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息：{}",category);

        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 查询所有菜品分类
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        log.info("查询菜品分类信息：{}",category.getType());

        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(category.getType() != null, Category::getType,category.getType());
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        List<Category> list = categoryService.list(lambdaQueryWrapper);
        return R.success(list);

    }

}
