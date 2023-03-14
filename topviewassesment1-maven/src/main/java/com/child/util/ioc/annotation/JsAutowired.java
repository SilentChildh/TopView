package com.child.util.ioc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用域字段上，可以在运行期间通过反射获取该注解。<br/>
 * <p/>
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsAutowired {

    /**
     * @return 要注入的class类型
     */
    Class<?> value() default Class.class;

    /**
     * @return bean的名称
     */
    String name() default "";
}