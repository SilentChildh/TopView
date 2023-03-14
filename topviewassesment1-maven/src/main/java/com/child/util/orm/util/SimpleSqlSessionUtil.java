package com.child.util.orm.util;

import com.child.util.ChildDataSource;
import com.child.util.ChildLogger;
import com.child.util.orm.SimpleSqlSessionFactory;
import com.child.util.orm.SqlSession;
import com.child.util.orm.SqlSessionFactory;
import com.child.util.orm.bean.MapperStatement;
import com.child.util.xml.ParseXMLUtils;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

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
        logger.info("创建会话工厂成功");
        // 返回一个工厂类
        return simpleSqlSessionFactory;
    }

    /**
     * 开启带有默认数据库环境{@code DEFAULT_DATASOURCE_ENVIRONMENT}的{@link SqlSession}实例，
     * 并默认手动提交事务。<br/>
     *
     * @return {@link SqlSession}
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    public static SqlSession openSession() throws SQLException {
        return openSession(DEFAULT_DATASOURCE_ENVIRONMENT, false);
    }

    /**
     * 通过指定数据库连接资源的全限定类名来得到对应的{@link SqlSession}，默认事务提交方式为手动提交。<br/>
     * <p/>
     *
     * @param resource 数据库连接资源的全限定类名
     * @return {@link SqlSession}
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    public static SqlSession openSession(String resource) throws SQLException {
        return openSession(resource, false);
    }

    /**
     * 通过指定数据库连接资源的全限定类名来得到对应的{@link SqlSession}。<br/>
     * <p/>
     * 注意该方法将会自动开启会话类中对数据库的连接。<br/>
     * @param resource   resource 数据库连接资源的全限定类名
     * @param autoCommit 提交事务的方式，true为自动提交，false为手动提交
     * @return {@link SqlSession}
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    public static SqlSession openSession(String resource, boolean autoCommit) throws SQLException {
        // 尝试从sqlSessionFactoryMap集合中获取工厂类
        SqlSessionFactory sqlSessionFactory = SQL_SESSION_FACTORY_MAP.get(resource);
        // 如果不存在该工厂类，那么就build一个出来
        if (sqlSessionFactory == null) {
            sqlSessionFactory = build(resource);
        }
        // 通过指定工厂获取会话资源并返回
        SqlSession sqlSession = sqlSessionFactory.openSession(autoCommit);
        // 开启连接
        sqlSession.openConnection();
        return sqlSession;
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
        // 用于接收SQL映射对象
        // noinspection AlibabaCollectionInitShouldAssignCapacity
        Map<String, MapperStatement> mapperStatementMap = new HashMap<>();

        // 获取XML文件
        List<File> xmlFiles = ParseXMLUtils.getXMLFileFromPackage(packageName);

        // 解析XML文件，并返回
        return ParseXMLUtils.parseMapper(xmlFiles);
    }

    /**
     * 返回指定目录路径下的xml文件中的SQL映射对象。<br/>
     * <p/>
     *
     * @param directoryPath 指定目录路径
     * @return Map<String, MapperStatement> 返回对应目录下的SQL映射对象，
     * K为SQL的全限定id，V为SQL映射对象
     */
    private static Map<String, MapperStatement> getStatementMapperFromDirectory(String directoryPath) {
        // 用于接收SQL映射对象
        // noinspection AlibabaCollectionInitShouldAssignCapacity
        Map<String, MapperStatement> mapperStatementMap = new HashMap<>();

        // 获取XML文件
        List<File> xmlFiles = ParseXMLUtils.getXMLFileFromDirectory(directoryPath);

        // 解析XML文件，并返回
        return ParseXMLUtils.parseMapper(xmlFiles);


    }
    private static final Logger logger = ChildLogger.getLogger();

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
