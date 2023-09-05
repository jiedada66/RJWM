package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.result.BaseContext;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    public void submit(Orders orders) {
        //查询用户id
        Long userId = BaseContext.getCurrentId();

        //查询购物车信息
        LambdaQueryWrapper<ShoppingCart> shoppingCartWrapper = new LambdaQueryWrapper<>();
        shoppingCartWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartWrapper);
        //判断购物车是否为空，为空则抛出异常
        if (shoppingCartList.isEmpty()){
            throw new RuntimeException("购物车为空,请先添加菜品");
        }

        //查询用户信息
        User user = userService.getById(userId);

        //查询用户地址
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        //判断地址是否为空，为空则抛出异常
        if (addressBook == null){
            throw new RuntimeException("地址为空,请先添加地址");
        }

        //订单号
        long number = IdWorker.getId();
        //总金额
        AtomicInteger amount = new AtomicInteger(0);//保证原子性，防止多线程并发，用int或long可能会出现并发问题

        //生成订单详情
        List<OrderDetail> orderDetailList = new ArrayList<>();
        shoppingCartList.forEach(shoppingCart -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(number);
            orderDetail.setNumber(shoppingCart.getNumber());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setDishId(shoppingCart.getDishId());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            orderDetail.setAmount(shoppingCart.getAmount());
            orderDetail.setImage(shoppingCart.getImage());
            orderDetail.setName(shoppingCart.getName());
            orderDetailList.add(orderDetail);
            //累加总金额
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
        });

        //生成订单
        orders.setId(number);
        orders.setNumber(String.valueOf(number));
        orders.setStatus(2);
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setAddressBookId(addressBookId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setPayMethod(1);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setRemark(orders.getRemark());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress((addressBook.getProvinceName() == null?"" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null?"" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null?"" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null?"" : addressBook.getDetail()));

        //插入订单
        this.save(orders);

        //插入订单详情，多条数据
        orderDetailService.saveBatch(orderDetailList);

        //删除购物车
        shoppingCartService.remove(shoppingCartWrapper);

    }

}
