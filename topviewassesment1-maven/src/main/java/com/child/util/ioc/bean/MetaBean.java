package com.child.util.ioc.bean;

import java.util.Map;

/**
 * 用于存储bean实例的相关信息。<br/>
 * <p/>
 * 该对于{@link MetaBean}来说，其实也是一个bean实例，只不过该bean实例不被IoC容器所管理。<br/>
 * 该实例仅用于保存实际应用中bean实例的相关信息，即可以理解为bean实例的元数据<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/14
 */
public class MetaBean {
    /**
     * id用于表示全限定类名
     */
    private String id;

    /**
     * 对应bean的class对象
     */
    private Class<?> clazz;

    /**
     * 保存对应bean实例每个字段的值set的值
     */
    private Map<String, String> properties;
    /**
     * 保存对应bean实例构造器的实参值
     */
    private Map<String, String> constructorArgs;

    public MetaBean(String id, Class<?> clazz,
                    Map<String, String> properties,
                    Map<String, String> constructorArgs) {
        this.id = id;
        this.clazz = clazz;
        this.properties = properties;
        this.constructorArgs = constructorArgs;
    }

    public Map<String, String> getConstructorArgs() {
        return constructorArgs;
    }

    public void setConstructorArgs(Map<String, String> constructorArgs) {
        this.constructorArgs = constructorArgs;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public MetaBean() {
    }

    public MetaBean(String id, Class<?> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }
}
