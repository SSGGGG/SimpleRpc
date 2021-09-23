package com.excelman.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Excelman
 * @date 2021/9/23 下午4:46
 * @description 添加在启动类上面
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceScan {

    /**
     * 该值定义为扫描基类的入口包，扫描的时候会找到该包以及子包中所有标记有@Service注解的类，并注册
     * @return
     */
    String packageName() default "";
}
