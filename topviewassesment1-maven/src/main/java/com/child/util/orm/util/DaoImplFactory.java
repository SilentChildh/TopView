package com.child.util.orm.util;

import com.child.util.orm.SqlSession;
import com.child.util.orm.annotation.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO层实现类的代理工厂。<br/>
 * <p/>
 * 可以通过{@code getDAOImplProxy(Class)}方法获取对应的DAO接口实现类。<br/>
 * 对于利用该工厂类获取的实现类，实际上并不会调用实现类的方法，而是调用工厂类的算法。
 * 该工常类需要的是实现类的相关信息，并不需要它们方法体内的算法。<br/>
 * 故实现类中可以不用编写方法体内的内容。</>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/13
 */
public class DaoImplFactory implements InvocationHandler {
    /**
     * 注解{@link Param}的类对象，将用于判断是否存在该注解。
     */
    private static final Class<? extends Annotation> PARAM_ANNOTATION = Param.class;

    /**
     * 对应DAO实现类的class对象
     */
    public Class<?> implClazz;

    /**
     * 获取对应DAO接口的实现类的代理类。<br/>
     * <p/>
     * 用户再获取代理类时，需要注意的是用DAO接口进行引用接收代理类。<b/>
     * 传入的参数则是对应DAO接口实现类的class对象。<br/>
     *
     * @param implClazz 对应DAO接口实现类的class对象
     * @return 对应的代理类
     */
    @SuppressWarnings("unchecked")
    public <T> T getDaoImplProxy(Class<? extends T> implClazz) {
        this.implClazz = implClazz;
        return (T) Proxy.newProxyInstance(implClazz.getClassLoader(), implClazz.getInterfaces(),this);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /*准备会话类相关配置*/
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();


        /*开始进行多参数转化为单参数过程*/

        // 用于存储原Map集合元素或者被注解修饰的参数。
        // noinspection AlibabaCollectionInitShouldAssignCapacity
        final Map<String, Object> parametersMap = new HashMap<>();
        // 用于接收单参数
        Object object;
        // 获取方法形参的数据
        Parameter[] parameters = method.getParameters();

        // 特判无参或者只有一个实参且无注解修饰时的情况
        boolean noneParameter = parameters.length == 0;
        boolean singleParameter = (!parameters[0].isAnnotationPresent(PARAM_ANNOTATION) && parameters.length == 1);
        if (noneParameter || singleParameter) {
            object = args[0];
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

            object = parametersMap;
        }

        String implName = implClazz.getName();
        int index = implName.indexOf("DAO");
        String sqlId = implName.substring(0, index + 3) + '.' + method.getName();
        Class<?> returnType = method.getReturnType();
        /*调用会话类的方法，对数据库执行CRUD操作*/
        if (returnType == int.class) {
            return sqlSession.update(sqlId, object);
        }
        else if (returnType == List.class) {
            return sqlSession.selectList(sqlId, object);
        }
        else {
            return sqlSession.selectOne(sqlId, object);
        }

    }
}
