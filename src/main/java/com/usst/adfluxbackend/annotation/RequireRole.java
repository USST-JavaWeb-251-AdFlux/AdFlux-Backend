package com.usst.adfluxbackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // 作用于方法
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
public @interface RequireRole {
    String value(); // 传入需要的角色，例如 "ADMIN", "ADVERTISER", "PUBLISHER"
}