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
    private MetaBean metaBean;
    /**
     * 存储所有bean实例的元信息，K为全限定id，V对应bean的元信息
     */
    public Map<String, MetaBean> metaBeanMap = new HashMap<>();

    public Map<String, MetaBean> getMetaBeanMap() {
        return metaBeanMap;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //if (qName.equals())
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    private static final String BEAN = "bean";
    private static final String ID = "id";
    private static final String NAME = "name";
    //private static final
}
