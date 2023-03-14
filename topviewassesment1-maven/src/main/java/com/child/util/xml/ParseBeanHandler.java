package com.child.util.xml;

import com.child.util.ioc.bean.MetaBean;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 解析保存相关bean实例的信息的xml文件。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/14
 */
public class ParseBeanHandler extends DefaultHandler {
    /**
     * bean元信息
     */
    private MetaBean metaBean;
    /**
     * bean的全限定id
     */
    private String id;
    /**
     * 保存对应bean实例每个字段的值
     */
    private Map<String, String> properties;
    /**
     * 保存对应bean实例构造器的实参值
     */
    private Map<String, String> constructorArgs;
    /**
     * 存储所有bean实例的元信息，K为全限定id，V对应bean的元信息
     */
    public Map<String, MetaBean> metaBeanMap = new HashMap<>();

    public Map<String, MetaBean> getMetaBeanMap() {
        return metaBeanMap;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (BEAN.equals(qName)) {
            // 新建一个MetaBean，以及两个Map
            metaBean = new MetaBean();
            properties = new HashMap<>();
            constructorArgs = new HashMap<>();

            // 设置全限定id
            id = attributes.getValue(ID);
            metaBean.setId(id);
        }
        if (PROPERTY.equals(qName)) {
            // 设置set值
            String name = attributes.getValue(NAME);
            String value = attributes.getValue(VALUE);
            properties.put(name, value);
        }

        if (CONSTRUCTOR_ARGS.equals(qName)) {

            String name = attributes.getValue(NAME);
            String value = attributes.getValue(VALUE);
            constructorArgs.put(name, value);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (BEAN.equals(qName)) {
            metaBean.setProperties(properties);
            metaBean.setConstructorArgs(constructorArgs);
            // 将metaBean放入Map中
            metaBeanMap.put(id, metaBean);
        }
    }


    private static final String BEAN = "bean";
    private static final String ID = "id";
    private static final String PROPERTY = "property";
    private static final String CONSTRUCTOR_ARGS = "constructor-args";
    private static final String NAME = "name";
    private static final String VALUE = "value";


}
