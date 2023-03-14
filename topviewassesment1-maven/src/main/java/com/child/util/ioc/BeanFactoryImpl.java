package com.child.util.ioc;

import com.child.util.ioc.bean.MetaBean;
import com.child.util.xml.ParseBeanHandler;
import com.child.util.xml.ParseXmlUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 引用数据类型的bean容器。<br/>
 * <p/>
 * 调用者将通过该方法获取所需的bean实例。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/14
 */

public class BeanFactoryImpl implements BeanFactory{
    private final Map<String, Object> beanMap = new HashMap<>();
    private final Map<String, MetaBean> metaBeanMap = new HashMap<>();

    /**
     * 通过对应的bean的class对象获取一个bean实例。<br/>
     *
     * @param clazz 对应的bean的class对象
     * @return {@link T} 返回一个实例，数据类型为引用类型
     */

    public <T> T getBean(Class<? extends T> clazz) {
        // 特判为null
        if (clazz == null) {
            return null;
        }

        // 尝试从beanMap容器中获取已有的bean实例
        String beanName = clazz.getName();
        if (beanMap.containsKey(beanName)) {
            return (T) beanMap.get(beanName);
        }

        // 否则进行返回null
        return null;
    }


    /**
     * 通过配置文件中的全限定类名将对应的bean注册到容器中。<br/>
     *
     * @param baseName 全限定类名
     * @return {@link Object} 返回一个bean实例
     */

    @Override
    public Object registerBean(String baseName) {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        try {
            saxParser = saxParserFactory.newSAXParser();
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException("解析bean文件失败");
        }
        ParseBeanHandler parseBeanHandler = new ParseBeanHandler();
        List<File> xmlFileFromPackage = ParseXmlUtils.getXmlFileFromPackage("");

        //saxParser.parse();
        return null;
    }
}

