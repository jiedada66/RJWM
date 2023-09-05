package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.result.BaseContext;
import com.itheima.reggie.result.R;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("用户下单：{}", orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 分页查询当前用户订单和详情
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("userPage")
    public R<Page<OrdersDto>> pageWithDetail(int page, int pageSize) {
        log.info("分页查询当前用户订单和详情");
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        //分页查询Orders
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getUserId,userId);
        lambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,lambdaQueryWrapper);

        Page<OrdersDto> pageInfoDto = new Page<>();
        //将pageInfo对象拷贝给pageInfoDto,但是不拷贝records属性
        BeanUtils.copyProperties(pageInfo,pageInfoDto,"records");
        //设置pageInfoDto的records属性
        List<OrdersDto> ordersDtoList = new ArrayList<>();
        pageInfo.getRecords().forEach(orders -> {
            OrdersDto ordersDto = new OrdersDto();
            //将orders对象拷贝给ordersDto
            BeanUtils.copyProperties(orders,ordersDto);
            //根据ordersId查询OrderDetail
            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderDetail::getOrderId,orders.getId());
            List<OrderDetail> list = orderDetailService.list(queryWrapper);
            ordersDto.setOrderDetails(list);
            ordersDtoList.add(ordersDto);
        });
        pageInfoDto.setRecords(ordersDtoList);

        return R.success(pageInfoDto);
    }

    /**
     * 分页查询订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, String number, String beginTime, String endTime) {
        log.info("分页查询订单");
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(number != null,Orders::getNumber,number)
                .ge(beginTime != null,Orders::getOrderTime,beginTime)
                .le(endTime != null,Orders::getOrderTime,endTime)
                .orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    public static void main(String[] args) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(date);
        LocalDateTime localDateTime = LocalDateTime.parse(format, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println(localDateTime);
    }
}
