package com.foodtime.handler;

import com.foodtime.constant.MessageConstant;
import com.foodtime.exception.BaseException;
import com.foodtime.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    //SQL
    //Duplicate entry '7230264239' for key 'employee.idx_username'
    @ExceptionHandler
    public Result exceptionHandler(Exception ex){
        String message = ex.getMessage();
        if (message != null && message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username = split[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else{
            log.error("未知异常：", ex);
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }

}
