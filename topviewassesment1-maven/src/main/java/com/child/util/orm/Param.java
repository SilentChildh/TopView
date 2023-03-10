package com.child.util.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于方法形参上，该注解之中的{@code value}应该是对应SQL语句中占位符"#{}"中的字面量。<br/>
 * 可在运行期间保留该注解，可以通过反射获取注解上的值。<br/>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
    String value();// 可在该注解上传入值
}
