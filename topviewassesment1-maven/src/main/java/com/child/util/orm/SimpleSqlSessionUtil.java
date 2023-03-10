package com.child.util.orm;

import com.child.util.ChildDataSource;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * orm的工具类。<br/>
 * <p/>
 * <ol>
 *     <li>首先，对于{@code sqlSessionFactoryMap}集合，
 *         如果仿照mybatis，那么就应该K为environment的id，V为工厂类。
 *     </li>
 *     <li>
 *         第二，对于{@code build()}方法，如果具有mybatis-config.xml配置文件的话，那么就需要解析更多的元素。
 *         特别是对于mapper.xml的路径位置，也会在config.xml文件声明。但是对于简易的orm来说，此处并没有config.cml文件，
 *         故所有的mapper.xml文件默认放在dao包之下，在{@code build()}方法内，将对dao包下的mapper.xml进行解析，
 *         并将解析得到的SQL映射存放到Mao集合中，最后传给每一个Factory实例中。
 *     </li>
 *     <li>
 *         第三，如果不是特殊要求的话，
 *         最好通过{@code openSession()}来开启会话，而不应该使用{@code build()}手动创建一个工厂类。
 *         因为{@code openSession()}方法将检查当前应用下，是否已经创建过相同的工厂类，进而保证每一个工厂类实例的作用域范围。
 *     </li>
 * </ol>
 *
 * @author silent_child
 * @version 1.0
 **/

public class SimpleSqlSessionUtil {
    /**
     * 该集合中保存了所有已存在的SqlSessionFactory实例。对于每一个工厂类应该都是保存不同数据的实例。<br/>
     * Key为对应数据库连接池配置文件的全限定类名，Value是根据数据库连接池创建的工厂类。<br/>
     */
    private static final Map<String, SqlSessionFactory> SQL_SESSION_FACTORY_MAP = new HashMap<>();

    /**
     * 根据配置文件创建一个{@code SqlSessionFactory}实例。<br/>
     * <p/>
     *
     * @param resource 对应数据库连接池配置文件的全限定类名
     * @return {@link SqlSessionFactory} 返回一个含有对应配置信息的工厂类
     */
    public static SqlSessionFactory build(String resource) {
        // 根据配置文件的全限定类名来创建数据库资源
        DataSource childDataSource = ChildDataSource.creatDataSource(resource);
        // 对mapper.xml进行解析，并接收原生SQL映射对象集合
        Map<String, MapperStatement> mapperStatementMap = getStatementMapperFromPackage(PACKAGE_NAME);

        // 创建工厂类
        SqlSessionFactory simpleSqlSessionFactory =
                new SimpleSqlSessionFactory(childDataSource, mapperStatementMap);
        // 将工厂类放入sqlSessionFactoryMap集合中统一管理
        SQL_SESSION_FACTORY_MAP.put(resource, simpleSqlSessionFactory);
        return simpleSqlSessionFactory;// 返回一个工厂类
    }

    /**
     * 开启带有默认数据库环境{@code DEFAULT_DATASOURCE_ENVIRONMENT}的{@link SqlSession}实例，
     * 并默认手动提交事务。<br/>
     *
     * @return {@link SqlSession}
     */
    public static SqlSession openSession() {
        return openSession(DEFAULT_DATASOURCE_ENVIRONMENT, false);
    }

    /**
     * 通过指定数据库连接资源的全限定类名来得到对应的{@link SqlSession}，默认事务提交方式为手动提交。<br/>
     * <p/>
     *
     * @param resource 数据库连接资源的全限定类名
     * @return {@link SqlSession} 返回一个会话类
     */
    public static SqlSession openSession(String resource) {
        return openSession(resource, false);
    }

    /**
     * 通过指定数据库连接资源的全限定类名来得到对应的{@link SqlSession}。<br/>
     * <p/>
     *
     * @param resource   resource 数据库连接资源的全限定类名
     * @param autoCommit 提交事务的方式，true为自动提交，false为手动提交
     * @return {@link SqlSession} 返回一个会话类
     */
    public static SqlSession openSession(String resource, boolean autoCommit) {
        // 尝试从sqlSessionFactoryMap集合中获取工厂类
        SqlSessionFactory sqlSessionFactory = SQL_SESSION_FACTORY_MAP.get(resource);
        // 如果不存在该工厂类，那么就build一个出来
        if (sqlSessionFactory == null) sqlSessionFactory = build(resource);
        // 通过指定工厂获取会话资源并返回
        return sqlSessionFactory.openSession(autoCommit);
    }

    /**
     * 将xml文件中的原生sql语句解析为一个符合JDBC规范的sql语句。<br/>
     * <p/>
     * 该方法适用于调用者传入的是一个pojo类实参时。<br/>
     * 该方法首先会利用正则表达式将xml文件中的原生sql语句进行占位符的替换，即将"#{}"替换为"?"。<br/>
     * 然后再通过反射对该字符串中涉及到parameters对象字段的字符进行替换，即从java规范命名替换为sql规范命名。<br/>
     *
     * @param prototypeSql 原生sql语句，将解析该语句为JDBC的SQL语句
     * @param parameters   需要执行sql操作的类对象
     * @return String 返回一个符合JDBC规范的sql语句
     */
/*    public static String parsePrototypeSql(String prototypeSql, Class<?> parameters) {
        // 正则表达式，用于将整个占位符"#{}"的所有内容替换为"?"
        String tempSql = prototypeSql.replaceAll("#\\{[a-zA-Z0-9_$]*}", "?");

        // 使用Builder包装转换一次后的sql语句，然后在二次转换时进行高效拼接
        StringBuilder sql = new StringBuilder(tempSql);

        Field[] fields = parameters.getDeclaredFields();// 获取parameters对象中的所有字段

        for (Field field : fields) {// 遍历所有字段
            String fieldName = field.getName();// 字段名
            int begin = 0;// 对sql进行查找的起始位置，每对一个字段查询完毕后，都会重置为0

            *//*开始对该字段在sql中出现的所有位置进行替换*//*
            while (true) {
                *//*接下来开始查找字段在sql中出现的位置，begin与end的关系是左闭右开*//*
                int attributePosition = sql.indexOf(fieldName, begin);
                if (attributePosition == -1) break;// 当查找不到该字段时，表明该字段在sql中已经替换完毕，故直接退出
                int end = attributePosition + fieldName.length();// 字段最后一个字母索引位置 + 1

                *//*接下来对在sql中查找到的字段进行替换*//*
                for (int i = attributePosition; i <= end; i++) {
                    char letter = sql.charAt(i);// 遍历得到字段的每一个字母
                    if (Character.isUpperCase(letter)) {// 如果字母是大写字母
                        // 替换为小写字母,并且在字母前加上字符'_'
                        sql.replace(i, i + 1, "_" + Character.toLowerCase(letter));
                    }
                }

                // 更新开始查询的索引位置
                begin = end;
            }
        }

        // 循环完毕，sql完成解析
        return sql.toString();// 将Builder再转换为String进行返回
    }*/

    /**
     * 将xml文件中的SQL语句转换为符合JDBC规范以及符合数据库表字段名规范的SQL语句。<br/>
     * <p/>
     * 该方法首先会利用正则表达式将xml文件中的原生sql语句进行占位符的替换，即将"#{}"替换为"?"。<br/>
     * 并另字段名从java规范命名替换为sql规范命名。<br/>
     *
     * @param prototypeSql 原生SQL语句
     * @return String 返回一个符合JDBC规范的sql语句
     */
    public static String parsePrototypeSql(String prototypeSql) {
        // 正则表达式，用于将整个占位符"#{}"的所有内容替换为"?"
        String tempSql = prototypeSql.replaceAll("#\\{[a-zA-Z0-9_$]*}", "?");
        StringBuilder sql = new StringBuilder(tempSql);// 对字符串进行操作
        String[] words = prototypeSql.split("[\\s,()]");// 以空格、逗号、括号分割

        for (int i = 0; i < words.length; i++) {
            for (int j = 0; j < words[i].length() - 1; j++) {
                // 如果前后两个字母大小写不一致，则进行替换
                if (Character.isLowerCase(words[i].charAt(j)) && Character.isUpperCase(words[i].charAt(j + 1))) {

                    // 替换之前的大写字母为 "_"和小写字母
                    String replace = "_" + Character.toLowerCase(words[i].charAt(j + 1));
                    StringBuilder newWord = new StringBuilder(words[i]);
                    newWord.replace(j + 1, j + 2, replace);

                    int wordIndex = sql.indexOf(words[i]);// 找到原sql中该单词的位置
                    if (wordIndex == -1) break;

                    sql.replace(wordIndex, wordIndex + words[i].length(), newWord.toString());// 对原SQL进行替换

                }
            }
        }

        return sql.toString();
    }


    /**
     * 传入连接资源、sql语句以及含有特定数据的pojo实例来为操作数据库数据进行准备，
     * 调用该方法将返回一个可以立即执行的{@link PreparedStatement}实例。<br/>
     * <p/>
     * 该方法将调用{@code parsePrototypeSql()}方法获取一个解析后的SQL，
     * 再通过该SQL与连接资源和Object实例资源配合进行操作。<br/>
     *
     * @param connection   指定的连接资源，用于创建实例
     * @param prototypeSql 写在xml文件中的原生SQL
     * @param parameters   含有特定数据，即为占位符"?"传值的数据
     * @return {@link PreparedStatement}
     * @throws SQLException 直接向上抛出
     */
    public static PreparedStatement sqlHandler(Connection connection, String prototypeSql, Object parameters) throws SQLException {
        Class<?> parametersClass = parameters.getClass();// 获取parameters的运行类型

        /*接下来开始解析原生sql为符合JDBC规范的sql语句*/
        String sql = SimpleSqlSessionUtil
                .parsePrototypeSql(prototypeSql);// 传入原生sql得到解析后的sql语句

        /*接下来解析原生sql中相关占位符中的对象属性信息保存在Map集合中*/
        Map<Integer, String> field = new HashMap<>();// 存放占位符"#{}"中查询得到的次序和属性名
        final String LEFT = "#{";// 占位符的左半边
        final String RIGHT = "}";// 占位符的右半边
        int begin;// 子串的起始位置
        int end;// 子串的最终位置
        int fromIndex = 0;// 开始查询字符串的位置
        int i = 1;// 记录占位符出现的次序
        while (true) {
            begin = prototypeSql.indexOf(LEFT, fromIndex) + 2;// 找到"#{"字符串后的第一个字符索引
            end = prototypeSql.indexOf(RIGHT, fromIndex);// 找到"}"字符的索引
            if (end <= 0) break;// 未找到则退出

            field.put(i++, prototypeSql.substring(begin, end));// 截取begin和end直接的字符串
            fromIndex = end + 1;// 将开始查询的位置设置为"}"字符后的第一个字符索引
        }

        /*将解析好的sql结合先前解析得到的Map集合为占位符"?"进行赋值*/
        final String GET = "get";// 用于拼接po类获取字段值的方法名
        // 创建preparedStatement实例
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        // 遍历，通过反射为每一个占位"?"进行赋值
        field.forEach((index, fieldName) -> {
            // 每一个占位符中的字段的get方法名
            String getMethodName = GET + (char) (fieldName.charAt(0) - 32) + fieldName.substring(1);
            try {
                // 通过反射获取get方法
                Method getMethod = parametersClass.getDeclaredMethod(getMethodName);
                // 调用get方法得到obj中的私有属性，然后给sql语句中的占位符?赋值
                preparedStatement.setObject(index, getMethod.invoke(parameters));

            } catch (NoSuchMethodException | SQLException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });

        // 返回赋完值的preparedStatement实例
        return preparedStatement;

    }

    /**
     * 传入连接资源、sql语句以及含有特定数据的Map实例来为操作数据库数据进行准备，
     * 调用该方法将返回一个可以立即执行的{@link PreparedStatement}实例。<br/>
     * <p/>
     * 该方法将调用{@code parsePrototypeSql()}方法获取一个解析后的SQL，
     * 再通过该SQL与连接资源和Object实例资源配合进行操作。<br/>
     *
     * @param connection   指定的连接资源，用于创建实例
     * @param prototypeSql 写在xml文件中的原生SQL
     * @param parameters   含有特定数据，即为占位符"?"传值的数据。K为占位符"#{}"中的字面量值，即属性名，V为要传入的实参值
     * @return {@link PreparedStatement}
     * @throws SQLException 直接向上抛出
     */
    public static PreparedStatement sqlHandler(Connection connection, String prototypeSql,
                                               Map<String, Object> parameters) throws SQLException {
        /*获取解析后的sql语句*/
        String sql = SimpleSqlSessionUtil.parsePrototypeSql(prototypeSql);

        /*接下来解析原生sql中相关占位符中的对象属性信息保存在Map集合中*/
        Map<Integer, String> field = new HashMap<>();// 存放占位符"#{}"中查询得到的次序和属性名
        final String LEFT = "#{";// 占位符的左半边
        final String RIGHT = "}";// 占位符的右半边
        int begin;// 子串的起始位置
        int end;// 子串的最终位置
        int fromIndex = 0;// 开始查询字符串的位置
        int i = 1;// 记录占位符出现的次序
        while (true) {
            begin = prototypeSql.indexOf(LEFT, fromIndex) + 2;// 找到"#{"字符串后的第一个字符索引
            end = prototypeSql.indexOf(RIGHT, fromIndex);// 找到"}"字符的索引
            if (end <= 0) break;// 未找到则退出

            field.put(i++, prototypeSql.substring(begin, end));// 截取begin和end直接的字符串
            fromIndex = end + 1;// 将开始查询的位置设置为"}"字符后的第一个字符索引
        }

        /*将解析好的sql结合先前解析得到的Map集合为占位符"?"进行赋值*/
        // 创建preparedStatement实例
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        // 遍历，通过两个Map集合为每一个占位"?"进行赋值
        field.forEach((index, fieldName) -> {
            try {
                // 得到需要传入的值
                Object obj = parameters.get(fieldName);
                // 给sql语句中的占位符?赋值
                preparedStatement.setObject(index, obj);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        return preparedStatement;
    }

    /**
     * 通过指定包名获取包下所有“.xml”为后缀的文件中的SQL映射对象。<br/>
     * <p/>
     *
     * @param packageName 指定包名，包下含有xml文件
     * @return Map<String, MapperStatement>返回包含dao包下所有的mapper.xml文件中对应的SQL映射对象集合。
     * K为全限定id，V为SQL映射对象。
     */
    public static Map<String, MapperStatement> getStatementMapperFromPackage(String packageName) {
        Map<String, MapperStatement> mapperStatementMap = new HashMap<>();// 用于接收SQL映射对象
        try {

            // 获取指定包下的所有文件的URL
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    // 注意！需要将包名各式转换为目录路径格式
                    .getResources(packageName.replace('.', '/'));
            // 遍历每一个URL
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();// 获取URL
                String protocol = url.getProtocol();// 获取协议
                if (FILE.equals(protocol)) {// 如何满足协议，则进行解析，并将解析后的元素合并到集合中
                    mapperStatementMap.putAll(getStatementMapperFromDirectory(url.getPath()));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 最后返回解析后的SQL映射对象
        return mapperStatementMap;
    }

    /**
     * 返回指定目录下的xml文件中的SQL映射对象。<br/>
     * <p/>
     * 根据目录名可以获取该目录下的所有xml文件，通过包名可以获取进一步解析子目录。<br/>
     * 其中核心部分为：
     * 解析处理器{@link ParseMapperHandler}帮助完成了将xml中的sql映射放置到Map集合中。<br/>
     * 其中为了提高效率，采用并行流的方式进行收集SQL映射对象到Map集合中。
     * 同时通过collect终结管道保证了线程安全。<br/>
     *
     * @param directoryName 指定包名的目录名
     * @return Map<String, MapperStatement> 返回对应目录下的SQL映射对象，
     * K为SQL的全限定id，V为SQL映射对象
     */
    private static Map<String, MapperStatement> getStatementMapperFromDirectory(String directoryName) {
        Map<String, MapperStatement> mapperStatementMap = new HashMap<>();// 用于接收SQL映射对象
        File file = new File(directoryName);// 根据目录创建文件
        // 如果不存在该目录则直接返回一个空的Map
        if (!file.isDirectory() || !file.exists()) return mapperStatementMap;

        // 获取目录下的所有".xml"结尾的文件
        File[] files = file.listFiles(fileName -> {
            if (fileName.isDirectory()) {// 如果存在子目录则继续搜索
                getStatementMapperFromDirectory(fileName.getAbsolutePath());
            }
            // 如果不是的话则直接返回后缀为".xml"的文件
            return fileName.getName().endsWith(".xml");

        });

        // 如果不存在".xml"结尾的文件则直接返回一个空Map
        if (files == null) return mapperStatementMap;

        /*接下来开始对所有".xml"文件进行解析*/
        try {
            // 获取解析器工厂
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            // 获取解析器
            SAXParser saxParser = saxParserFactory.newSAXParser();
            // 获取解析器处理器
            ParseMapperHandler parseMapperHandler = new ParseMapperHandler();

            return Arrays.stream(files).parallel()
                    // 将流中的.xml文件进行解析，返回SQL映射集合的K-V条目
                    .map(x -> {
                        try {
                            saxParser.parse(x, parseMapperHandler);
                            // 返回K-V条目回到流中
                            return parseMapperHandler.getStatementMapper().entrySet();
                        } catch (SAXException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    // 将Set<Entry<String, String>>一对多映射为Entry<String, String>，即从Set集合中取出元素
                    .flatMap(Set::stream)
                    // 最后通过线程安全的终结管道操作，将流中元素包装进Map集合中进行返回
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 默认数据库连接池配置地址，即数据库环境地址。
     */
    private static final String DEFAULT_DATASOURCE_ENVIRONMENT = "default-config";
    /**
     * 默认存放Mapper.xml文件的全限定包名
     */
    private static final String PACKAGE_NAME = "com.child.dao";
    /**
     * 协议类型常量
     */
    private static final String FILE = "file";
}
