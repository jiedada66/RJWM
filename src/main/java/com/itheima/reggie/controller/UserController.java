package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.result.R;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.util.SMSUtils;
import com.itheima.reggie.util.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user) throws Exception {
        //获取手机号
        String phone = user.getPhone();
        log.info("发送短信验证码：{}", phone);

        if (phone != null) {
            //生成验证码
            String code = ValidateCodeUtils.generateValidateCode4String(4);
            log.info("生成验证码：{}", code);
            //阿里云短信发送验证码
            // SMSUtils.sendShortMessage(phone, code);
            //将验证码和手机号存入redis
            jedisPool.getResource().setex(phone, 60 * 5, code);
            return R.success("验证码发送成功   ");
        }

        return R.error("验证码发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        //获取用户输入手机号
        String phone = (String) map.get("phone");
        //获取用户输入验证码
        String code = (String) map.get("code");
        //从redis中获取验证码
        String redisCode = jedisPool.getResource().get(phone);
        //对比验证码
        if (code.equals(redisCode)) {
            //验证码正确
            //判断用户是否是新用户
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(lambdaQueryWrapper);
            if (user == null) {
                //新用户
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            //登陆成功，将用户id存入session
            session.setAttribute("user", user.getId());
            return R.success(user);
        }
        //登录失败
        return R.error("验证码错误或已过期");
    }

    /**
     * 退出登录
     * @param session
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginOut(HttpSession session) {
        log.info("退出登录,清除session中的用户信息:{}", session.getAttribute("user"));
        session.removeAttribute("user");
        return R.success("退出成功");
    }

}
