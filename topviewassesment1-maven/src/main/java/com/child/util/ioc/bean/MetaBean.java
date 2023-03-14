package com.child.util.ioc.bean;

import java.util.Objects;

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
     * 对应bean的类
     */
    private Class<?> clazz;

    @Override
    public String toString() {
        return "MetaBean{" +
                "id='" + id + '\'' +
                ", clazz=" + clazz +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaBean metaBean = (MetaBean) o;
        return Objects.equals(id, metaBean.id) && Objects.equals(clazz, metaBean.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clazz);
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
