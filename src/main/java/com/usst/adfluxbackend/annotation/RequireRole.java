package com.usst.adfluxbackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // 作用于方法和类
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
public @interface RequireRole {
    // 传入需要的角色，例如 "admin", "advertiser", "publisher"
    // 为空时，表示需要是登录用户，但不检验角色
    String value() default "";
    boolean disabled() default false;
}