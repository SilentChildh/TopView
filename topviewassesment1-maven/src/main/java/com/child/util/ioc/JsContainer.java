package com.child.util.ioc;


/**
 * 该容器将装载被{@link JsContainer}修饰bean示例。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/13
 */
public interface JsContainer {

    /**
     * 根据Class对象获取Bean示例。<br/>
     * <p/>
     * 该方法一般用于业务层中，用于获取一个已注册到容器中的实例。<br/>
     *
     * @param clazz 指定的class对象
     * @return 返回指定类型的示例
     */
    <T> T getBean(Class<T> clazz);

    /**
     * 注册一个bean示例到容器中。
     *
     * @param clazz 一个指定的class对象
     * @return {@link Object} 返回一个已注册到容器中的bean实例
     */
    Object registerBean(Class<?> clazz);

    /**
     * 初始化容器.<br/>
     * <p/>
     * 在应用启动时，并在注册bean之后，应该调用该方法实现初始化操作。<br/>
     */
    void initAutoWired();
}