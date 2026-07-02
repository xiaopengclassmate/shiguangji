package com.foodtime.aspect;

import com.foodtime.annotation.AutoFill;
import com.foodtime.context.BaseContext;
import com.foodtime.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;


@Component
@Aspect
@Slf4j
public class AutoFillAspect {
    /*切入点*/
    @Pointcut("execution(* com.foodtime.mapper.*.*(..))&& @annotation(com.foodtime.annotation.AutoFill)")

    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行数据填充");


        //获取到当前被拦截的方法上的数据库操作类型
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
         AutoFill autoFill =signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType=autoFill.value();//获取数据库操作类型

        //获取到当前被拦截的方法的参数==实体对象
Object[] args = joinPoint.getArgs();
if(args ==null||args.length==0){
    return;

}
Object object = args[0];

        //准备赋值的信息
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();


        //根据当前不同的操作类型，为对应的属性通过反射来赋值
if(operationType== OperationType.INSERT){
    try {
        Method setUpdateTime = object.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
        Method setUpdateUser = object.getClass().getDeclaredMethod("setUpdateUser", Long.class);
        Method setCreateTime = object.getClass().getDeclaredMethod("setCreateTime", LocalDateTime.class);
        Method setCreateUser = object.getClass().getDeclaredMethod("setCreateUser", Long.class);
        setUpdateTime.invoke(object,now);
        setUpdateUser.invoke(object,currentId);
        setCreateTime.invoke(object,now);
        setCreateUser.invoke(object,currentId);

    }catch (Exception e) {
        e.printStackTrace();
    }
}else if(operationType== OperationType.UPDATE){
    try {
        Method setUpdateTime = object.getClass().getDeclaredMethod("setUpdateTime", LocalDateTime.class);
        Method setUpdateUser = object.getClass().getDeclaredMethod("setUpdateUser", Long.class);
        setUpdateTime.invoke(object,now);
        setUpdateUser.invoke(object,currentId);

    }catch (Exception e) {
        e.printStackTrace();
    }
}




    }

}
