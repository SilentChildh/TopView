package com.child.util.orm;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于在执行CRUD操作前，将外界传入的多参数转换为单参数，即Map或者Object实例.
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/12
 */

public class ParametersHandler {
    /**
     * 注解{@link Param}的类对象，将用于判断是否存在该注解。
     */
    private static final Class<? extends Annotation> PARAM_ANNOTATION = Param.class;
    /**
     * 指定方法的方法名
     */
    private String methodName;

    /**
     * 指定类的类对象，该类与应持有指定的方法
     */
    private Class<?> clazz;
    /**
     * 实参参数数组
     */
    private Object[] args;


    /**
     * 创建一个{@link ParametersHandler}
     *
     * @param methodName 指定方法的方法名
     * @param clazz      指定类的类对象，该类与应持有指定的方法
     * @param args       实参参数数组
     */
    public ParametersHandler(String methodName, Class<?> clazz, Object[] args) {
        this.methodName = methodName;
        this.clazz = clazz;
        this.args = args;
    }

    /**
     * 将多参数转换为单参数。<br/>
     *
     * @return {@link Object} 返回一个单参数
     */
    public Object handle() {
        // 得到对应的方法
        Method method = this.findMethod();
        // 用于存储原Map集合元素或者被注解修饰的参数。
        // noinspection AlibabaCollectionInitShouldAssignCapacity
        final Map<String, Object> parametersMap = new HashMap<>();
        // 获取方法形参的数据
        Parameter[] parameters = method.getParameters();

        // 特判无参或者只有一个实参且无注解修饰时的情况
        boolean noneParameter = parameters.length == 0;
        boolean singleParameter = (!parameters[0].isAnnotationPresent(PARAM_ANNOTATION) && parameters.length == 1);
        if (noneParameter || singleParameter) {
            return args[0];
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

    /**
     * 通过指定类以及方法名找到类中的方法。<br/>
     * <p/>
     * 当找到方法是返回{@link Method}，否则直接抛出异常。<br/>
     *
     * @return {@link Method}
     */
    private Method findMethod() {
        // 获取指定类的所有方法
        Method[] declaredMethods = clazz.getDeclaredMethods();

        for (Method declaredMethod : declaredMethods) {
            // 如果找到该方法则返回
            if (methodName.equals(declaredMethod.getName())) {
                return declaredMethod;
            }
        }
        // 否则直接抛出异常
        throw new RuntimeException("未找到指定方法");
    }
}
