package com.itheima.reggie.result;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody //返回json
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception) {
        log.error(exception.getMessage());

        if (exception.getMessage().contains("Duplicate entry")){
            String[] split = exception.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    /**
     * 自定义业务异常处理方法
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException exception) {
        log.error(exception.getMessage());

        return R.error(exception.getMessage());
    }


    /**
     * 捕获其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public R<String> exceptionHandler(Exception exception) {
        log.error(exception.getMessage());

        return R.error("未知错误");
    }

}
