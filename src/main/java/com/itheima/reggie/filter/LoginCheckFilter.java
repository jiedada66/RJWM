package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.result.BaseContext;
import com.itheima.reggie.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 * 名称随意
 */
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
@Component
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;

        //1.获取本次请求的uri
        String requestURI = request.getRequestURI();

        log.info("拦截到请求：{}",requestURI);

        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/backend/**",
                "/front/**",
                "/user/login",
                "/user/sendMsg"
        };

        //2.判断本次请求是否需要处理
        boolean check = check(urls,requestURI);

        //3.如果不需要处理就直接放行
        if (check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //4.1判断员工是否为登录状态，如果已经登录了就直接放行
        Object employee = request.getSession().getAttribute("employee");
        if (employee != null) {
            log.info("用户已登录，id为：{}",employee);

            Long empId = (Long) employee;
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        //4.2判断用户是否为登录状态，如果已经登录了就直接放行
        Object user = request.getSession().getAttribute("user");
        if (user != null) {
            log.info("用户已登录，id为：{}",user);
            Long userId = (Long) user;
            BaseContext.setCurrentId(userId);//设置当前线程的用户id

            filterChain.doFilter(request,response);
            return;
        }

        //5.如果没有登录就返回未登录结果,通过输出流方式向客户端响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

    }

    /**
     * 路径匹配，检查本次请求是否要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
