package com.child.util.orm.util;

import com.child.util.orm.SqlSession;
import com.child.util.orm.annotation.Param;
import com.child.util.orm.handler.ParametersHandler;

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
    private Class<?> clazz;

    /**
     * 获取对应DAO接口的实现类的代理类。<br/>
     * <p/>
     * 用户再获取代理类时，需要注意的是用DAO接口进行引用接收代理类。<b/>
     * 传入的参数则是对应DAO接口实现类的class对象。<br/>
     * @param clazz 对应DAO接口实现类的class对象
     * @return 对应的代理类
     * @param <T> DAO接口的泛型类型
     */
    @SuppressWarnings("unchecked")
    public <T> T getDaoImplProxy(Class<T> clazz) {
        this.clazz = clazz;
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},this);
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /*准备会话类相关配置*/
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();


        /*开始进行多参数转化为单参数过程*/

        // 用于存储原Map集合元素或者被注解修饰的参数。
        // noinspection AlibabaCollectionInitShouldAssignCapacity
        final Map<String, Object> parametersMap = new HashMap<>();

        ParametersHandler parametersHandler = new ParametersHandler(method.getName(), clazz, args);
        Object handle = parametersHandler.handle();

        String implName = clazz.getName();
        int index = implName.indexOf("DAO");
        String sqlId = implName.substring(0, index + 3) + '.' + method.getName();
        Class<?> returnType = method.getReturnType();
        /*调用会话类的方法，对数据库执行CRUD操作*/
        if (returnType == int.class) {
            return sqlSession.update(sqlId, handle);
        }
        else if (returnType == List.class) {
            return sqlSession.selectList(sqlId, handle);
        }
        else {
            return sqlSession.selectOne(sqlId, handle);
        }

    }
}
