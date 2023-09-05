package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.result.BaseContext;
import com.itheima.reggie.result.R;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 新增购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        log.info("shoppingCart:{}",shoppingCart);

        //设置用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //判断添加到购物车的是菜品还是套餐
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if (shoppingCart.getDishId() !=null) {
            //是菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        } else {
            //是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //添加购物车之前，先判断购物车中是否已经存在该菜品或套餐
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if (one != null) {
            //购物车中已经存在该菜品或套餐，只需要修改数量加1
            one.setNumber(one.getNumber() + 1);
            shoppingCartService.updateById(one);
            return R.success(one);
        } else {
            //购物车中不存在该菜品或套餐，直接添加
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
        }

        return R.success(shoppingCart);
    }

    /**
     * 查询购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        log.info("查询购物车");
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车所有商品
     * @return
     */
    @DeleteMapping("clean")
    public R<String> clean() {
        log.info("清空购物车");
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

    /**
     * 删除购物车中的某个商品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {
        log.info("shoppingCart:{}",shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //判断这个商品是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        Long setMealId = shoppingCart.getSetmealId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        if (dishId != null) {
            //是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        } else {
            //是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,setMealId);
        }
        //查询该商品的数量
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if (one.getNumber() > 1) {
            //数量大于1，修改数量减1
            one.setNumber(one.getNumber() - 1);
            shoppingCartService.updateById(one);
        } else {
            //数量等于1，删除该商品
            shoppingCartService.remove(queryWrapper);
        }
        return R.success("删除商品成功");

    }

}
