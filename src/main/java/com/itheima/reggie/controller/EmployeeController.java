package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.result.R;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //1.将输入的密码进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据输入的账户查询数据库是否存在
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername,employee.getUsername());
        Employee one = employeeService.getOne(wrapper);

        //3.判断
        if (one == null) {
            return R.error("登录失败");
        }

        //4.比对密码
        if (!one.getPassword().equals(password)) {
            return R.error("密码错误");
        }

        //5.是否禁用
        if (one.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        //6.登录成功，id存入session
        request.getSession().setAttribute("employee",one.getId());
        return R.success(one);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理session中保存的当前登录id
        log.info("员工退出，清理session中保存的当前登录id:{}",request.getSession().getAttribute("employee"));
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}",employee.toString());

        //设置初始密码为123456，需要设置md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

        //获取当前登录用户id
//        Long id = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(id);
//        employee.setUpdateUser(id);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> Page(int page,int pageSize,String name) {
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        //分页构造器
        Page pageInfo = new Page(page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name); //StringUtils.isNotEmpty(name)判断name是否为空，不为空则添加过滤条件，为空则不添加过滤条件

        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee) {
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为：{}",id);
//        Long id = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(id);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查找员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getByid(@PathVariable Long id) {
        log.info("根据id查找员工信息...");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("查询失败！");
    }

}
