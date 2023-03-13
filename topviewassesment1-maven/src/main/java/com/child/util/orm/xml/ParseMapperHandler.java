package com.child.util.orm.xml;

import com.child.util.orm.bean.MapperStatement;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * 作为解析器处理器，用于对每个mapper.xml文件进行解析，最终得到的CRUD标签数据将存放在{@code map}中。
 * 调用者可以通过{@code getMap()}方法获取该{@code map}集合。
 * @author silent_child
 * @version 1.0
 **/

public class ParseMapperHandler extends DefaultHandler {
    /**
     * mapper.xml文件中每个标签的数据
     */
    private MapperStatement mapperStatement;
    /**
     * mapper.xml文件中的命名空间
     */
    private String namespace;
    /**
     * 用于保存每个mapper.xml文件中的映射语句的数据，
     * K为SQL映射对象的全限定id，V为SQL映射对象
     */
    private final Map<String, MapperStatement> statementMapper = new HashMap<>();

    /**
     * 用于创建解析器
     */
    public ParseMapperHandler() {
    }

    /**
     * 返回一个保存mapper.xml文件中映射语句的集合。<br/>
     * <p/>
     * @return  Map<String, MapperStatement> Key为每条crud标签的全限定id，Value为每条curd标签中的属性、元素
     */
    public Map<String, MapperStatement> getStatementMapper() {
        return statementMapper;
    }

    /**
     * 获取标签头，可以获取标签头的内的属性。<br/>
     * <p/>
     * 需要注意的是，每当进入一个新的CURD标签时，就应该新建一个SQL映射对象。以便存储新的SQL相关信息。<br/>
     * 会将误操作填写属性值时出现的前后空格进行删除。<br/>
     * 但是需要注意的是，对于resultType属性来说，可能在mapper.xml文件并未填写，
     * 那么届时搜索该属性时将返回null，故不可再调用{@link String#trim()}方法<br/>
     *
     * @param uri The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param qName The qualified name (with prefix), or the
     *        empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {

        // 匹配mapper标签，将标签内属性namespace，赋值给本类的私有属性namespace
        if (QualifiedName.MAPPER.equals(qName)) {
            namespace = attributes.getValue(QualifiedName.NAMESPACE).trim();
        }
        else {
            // 每得到一个新的CRUD标签就新建一个映射对象
            mapperStatement = new MapperStatement();

            // 接下来四条语句为映射对象中的sqlType进行赋值
            if (QualifiedName.INSERT.equals(qName)) {
                mapperStatement.setSqlType(QualifiedName.INSERT);
            }
            else if (QualifiedName.UPDATE.equals(qName)) {
                mapperStatement.setSqlType(QualifiedName.UPDATE);
            }
            else if (QualifiedName.DELETE.equals(qName)) {
                mapperStatement.setSqlType(QualifiedName.DELETE);
            }
            else if (QualifiedName.SELECT.equals(qName)) {
                mapperStatement.setSqlType(QualifiedName.SELECT);
            }
            // 获取标签内属性id，与namespace进行拼接得到sql语句的映射位置,并且给映射对象中的sqlId赋值
            String sqlId = namespace + '.' + attributes.getValue(QualifiedName.ID).trim();
            mapperStatement.setSqlId(sqlId);
            // 最后对返回值类型属性赋值
            String resultType = attributes.getValue(QualifiedName.RESULT_TYPE);
            mapperStatement.setResultType(resultType);

        }
    }

    /**
     * 当解析到标签末尾时将SQL映射对象放入Map集合中<br/>
     * <p/>
     * 需要注意的是，每当放入一个SQL映射对象之后应该将引用置为null，否则后续的解析将会失败。<br/>
     * @param uri The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param localName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param qName The qualified name (with prefix), or the
     *        empty string if qualified names are not available.
     */
    @Override
    public void endElement(String uri, String localName, String qName) {
        // 匹配crud标签，将全限定id和映射对象放入hashMap中
        if (!QualifiedName.MAPPER.equals(qName)) {
            statementMapper.put(mapperStatement.getSqlId(), mapperStatement);
            // 每当解析完一个SQL映射对象之后，将引用指向null
            mapperStatement = null;
        }
    }

    /**
     * 将原生sql语句赋值给SQL映射对象。<br/>
     * <p/>
     * 需要注意的是，当未进入CRUD标签时，那么SQL映射对象为null.<br/>
     * @param ch The characters.
     * @param start The start position in the character array.
     * @param length The number of characters to use from the
     *               character array.
     * @throws SAXException
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // 为映射对象中的prototypeSql赋值，即赋值xml文件中的原生sql语句
        String value = new String(ch, start, length).trim();
        // 如果不为null，说明此时已经进入CRUD标签内
        if (mapperStatement != null) {
            mapperStatement.setPrototypeSql(value);
        }
    }

    static class QualifiedName {
        private static final String MAPPER = "mapper";
        private static final String INSERT = "insert";
        private static final String UPDATE = "update";
        private static final String DELETE = "delete";
        private static final String SELECT = "select";
        private static final String NAMESPACE = "namespace";
        private static final String ID = "id";
        private static final String RESULT_TYPE = "resultType";
    }
}
