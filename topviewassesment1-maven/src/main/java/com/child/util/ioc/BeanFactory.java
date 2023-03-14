package com.child.util.ioc;

/**
 * bean工厂，用于获取已注册到容器中的bean实例。<br/>
 * <p/>
 * 对于每个Factory的作用域应该是应用域。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/14
 */
public interface BeanFactory {
    /**
     * 通过对应的bean的class对象获取一个bean实例。<br/>
     *
     * @param clazz 对应的bean的class对象
     * @return {@link T} 返回一个实例，数据类型为引用类型
     */
    <T> T getBean(Class<? extends T> clazz);

    /**
     * 通过配置文件中的全限定类名将对应的bean注册到容器中。<br/>
     *
     * @param baseName 全限定类名
     * @return {@link Object} 返回一个bean实例
     */
    Object registerBean(String baseName);

}
