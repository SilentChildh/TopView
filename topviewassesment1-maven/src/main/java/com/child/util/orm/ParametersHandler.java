package com.child.util.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于在执行CRUD操作前，将外界传入的多参数转换为单参数，即Map或者Object实例
 * @author silent_child
 * @version 1.0
 **/

public class ParametersHandler implements InvocationHandler {
    /**
     * 注解{@link Param}的类对象，将用于判断是否存在该注解。
     */
    private final Class<? extends Annotation> PARAM_ANNOTATION = Param.class;

    /**
     * 将多参数转换为单参数。
     * @return {@link Object}, 但实际上有可能是 {@code Map<String, Object>} 或者 其他pojo实例
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        //用于存储原Map集合元素或者被注解修饰的参数。
        final Map<String, Object> parametersMap = new HashMap<>();
        // 获取方法形参的数据
        Parameter[] parameters = method.getParameters();

        // 特判无参或者只有一个实参且无注解修饰时的情况
        if (parameters.length == 0 || (!parameters[0].isAnnotationPresent(PARAM_ANNOTATION) && parameters.length == 1)) {
            return args[0];// 调用方法
        }
        // 否则进行是否带有的注解判断
        else {

            // 如果第一个实参是Map，则将其元素放入代理类中的Map集合中
            if (args[0] instanceof Map) {
                parametersMap.putAll((Map<String, Object>) args[0]);
            }
            // 如果实参不是Map，则判断是否有注解
            else {
                for (int i = 0; i < parameters.length; i++) {
                    // 判断是否被Param注解修饰
                    if (parameters[i].isAnnotationPresent(PARAM_ANNOTATION)) {
                        // 如果是，就获取该注解中的值作为K，传入的实参作为V，然后将其放入Map中
                        Param param = (Param) parameters[i].getAnnotation(PARAM_ANNOTATION);
                        parametersMap.put(param.value(), args[i]);
                    }
                }
            }

            return parametersMap;

        }
    }
}
