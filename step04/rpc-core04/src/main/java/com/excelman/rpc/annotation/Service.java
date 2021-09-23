package com.excelman.rpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Excelman
 * @date 2021/9/23 下午4:29
 * @description 表示一个服务具体实现类
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {

    /**
     * 标注为该服务接口的完整类名
     * @return
     */
    String name() default "";
}
