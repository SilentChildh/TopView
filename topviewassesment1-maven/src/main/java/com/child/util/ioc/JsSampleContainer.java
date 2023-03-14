package com.child.util.ioc;

import com.child.util.ioc.JsContainer;
import com.child.util.ioc.annotation.JsAutowired;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 容器应该作为应用域对象，一个应用上应只存在一个bean容器。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/13
 */
@SuppressWarnings("unchecked")
public class JsSampleContainer implements JsContainer {
    /**
     * 保存所有bean对象，格式为 com.xxx.xxx.XxxClass : @56x2ya
     */
    private Map<String, Object> beanNameMap;

    /**
     * 存储bean和name的关系
     */
    private Map<String, String> beanKeys;

    /**
     * 调用已开启一个应用域的容器。
     */
    public JsSampleContainer() {
        this.beanNameMap = new ConcurrentHashMap<>();
        this.beanKeys = new ConcurrentHashMap<>();
    }


    /**
     * 根据Class对象获取Bean示例。<br/>
     * <p/>
     * 该方法一般用于业务层中，用于获取一个已注册到容器中的实例。<br/>
     *
     * @param clazz 指定的class对象
     * @return {@link T}
     */
    @Override
    public <T> T getBean(Class<T> clazz) {
        String name = clazz.getName();
        Object object = beanNameMap.get(name);
        if(null != object){
            return (T) object;
        }
        return null;
    }


    /**
     * 注册一个bean示例到容器中。<br/>
     * <p/>
     * 应用启动时，应该获取对应的bean的Class对象，以便注册到容器中。<br/>
     * @param clazz clazz
     * @return {@link Object}
     */
    @Override
    public Object registerBean(Class<?> clazz) {
        String name = clazz.getName();

        beanKeys.put(name, name);

        Object bean = newInstance(clazz);

        beanNameMap.put(name, bean);

        return bean;
    }


    /**
     * 初始化自动连接
     */
    @Override
    public void initAutoWired() {
        beanNameMap.forEach((k,v) -> injection(v));
    }

    /**
     * 注入对象
     * @param object
     */
    public void injection(Object object) {
        // 所有字段
        try {
            // 获取对应class对象的所有字段
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                // 获取字段上的指定注解
                JsAutowired jsAutowired = field.getAnnotation(JsAutowired.class);
                if (null == jsAutowired) {

                }
                // 如果存在
                if (null != jsAutowired) {
                    // 要注入的字段
                    Object autoAutoWiredField = null;
                    // 获取注解上的值
                    String name = jsAutowired.name();

                    // 如果注解为默认值
                    if(name.equals("")){
                        // 判断注入的类型 是否是类Class类型 默认值也是Class
                        if(jsAutowired.value() == Class.class){
                            // 注册
                            autoAutoWiredField = register(field.getType());

                        } else {
                            // 指定装配的类
                            autoAutoWiredField = this.getBean(jsAutowired.value());

                            if (null == autoAutoWiredField) {
                                autoAutoWiredField = register(jsAutowired.value());
                            }
                        }
                    }
                    // 如果注解赋上其他值
                    else {
                        // 指定了特定的name
                        String className = beanKeys.get(name);
                        if(null != className && !className.equals("")){
                            autoAutoWiredField = beanNameMap.get(className);
                        }

                        if (null == autoAutoWiredField) {
                            throw new RuntimeException("Unable to load " + name);
                        }

                    }

                    // 不存在，则抛出异常
                    if (null == autoAutoWiredField) {
                        throw new RuntimeException("Unable to load " + field.getType().getCanonicalName());
                    }

                    /*为该对象的字段进行赋值*/
                    boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    field.set(object, autoAutoWiredField);
                    field.setAccessible(accessible);
                }
            }

        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * 私有方法，注册对应的bean实例。<br/>
     * <p/>
     * 主要用于特判class对象是否为null，然后再调用方法{@code registerBean()}。<br/>
     *
     * @param clazz clazz
     * @return {@link Object}
     */
    private Object register(Class<?> clazz){
        if(null != clazz){
            return this.registerBean(clazz);
        }
        return null;
    }

    /**
     * 创建对应class的对象，即bean实例。<br/>
     * @param clazz class对象
     * @return
     */
    public static Object newInstance(Class<?> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}