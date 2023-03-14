package com.child.util.orm;

import com.child.util.ChildLogger;
import com.child.util.orm.bean.ForPreparedStatement;
import com.child.util.orm.bean.MetaMapperStatement;
import com.child.util.orm.handler.*;

import java.sql.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * 该sql会话主要用于对数据库进行一系列的操作。集成了操作数据库必要的功能。<br/>
 * <p/>
 * 其中{@code close()},{@code commit()},{@code rollback()}方法主要在service层被调用。<br/>
 * 其中有关CRUD的操作均在dao层被调用。<br/>
 * 该类中的异常均向上抛出，交由调用者进行处理。<br/>
 * SqlSession的作用域应该是局部的，即用完就应该销毁。<br/>
 * 如果外界调用了无参构造器{@code SimpleSqlSession()}，
 * 那么应该再次调用{@code createSimpleSqlSession()}方法以获取一个完整的会话类实例。<br/>
 *
 * @author silent_child
 * @version 1.0
 **/
public class SimpleSqlSession implements SqlSession {
    /**
     * 每一个SqlSession实例都将持有一个<strong>唯一</strong>的事务管理器，用于管理事务相关的操作。
     */
    private Transaction transaction;

    /**
     * 从transaction实例中获取到的连接资源，对于每一个会话类，应该仅持有<strong>唯一</strong>的一份连接资源，
     * 并通过该连接资源完成整个会话关于数据库的操作，而不应该频繁的通过transaction对连接资源进行获取或者关闭。
     */
    private Connection connection;
    /**
     * 每一个sqlSession实例将拥有{@link SimpleSqlSessionFactory}类中所有SQL语句映射的访问权限。
     * K为sql语句全限定id，V为包含标签信息的对象
     */
    private Map<String, MetaMapperStatement> statementMap;
    /**
     * 每次调用CRUD操作时，都会进行拦截判断传入的实参类型，从而选择不同的SQL处理器。<br/>
     * 主要的两种处理器为{@link MapSqlHandler}和{@link ObjectSqlHandler}.<br/>
     */
    private SqlHandler sqlHandler;

    /**
     * 创建一个SqlSession对象，可以进行都数据库的操作。
     *
     * @param transaction  事务管理器
     * @param statementMap 包含SQL映射语句的集合
     * @return {@link SimpleSqlSession}
     */
    public SimpleSqlSession(Transaction transaction, Map<String, MetaMapperStatement> statementMap) {
        this.transaction = transaction;
        this.statementMap = statementMap;
    }


    /**
     * 用于关闭会话。<br/>
     * <p/>
     * 内部将会把连接释放。
     */
    @Override
    public void close() throws SQLException {
        transaction.close();
    }

    /**
     * 提交事务。
     */
    @Override
    public void commit() throws SQLException {
        transaction.commit();
    }

    /**
     * 回滚事务。
     */
    @Override
    public void rollback() throws SQLException {
        transaction.rollback();
    }

    /**
     * 用于插入parameters对象记录，返回受影响行数。
     * <p/>
     * 调用本类中的{@code update()}方法进行添加操作。<br/>
     *
     * @param sqlId      sql语句的一个映射，即sql语句的位置。
     * @param parameters 需要插入的对象
     * @return int 返回受影响行数
     */
    @Override
    public int insert(String sqlId, Object parameters) throws SQLException {
        return update(sqlId, parameters);
    }

    /**
     * 用于删除parameters对象记录，返回受影响行数。
     * <p/>
     * 调用本类中的{@code update()}方法进行删除操作。<br/>
     *
     * @param sqlId      sql语句的一个映射，即sql语句的位置。
     * @param parameters 需要删除的对象
     * @return int 返回受影响行数
     */
    @Override
    public int delete(String sqlId, Object parameters) throws SQLException {
        return update(sqlId, parameters);
    }

    /**
     * 用于更新parameters对象记录，返回受影响行数。
     * <p/>
     * 将自动开启连接。<br/>
     * 该方法作为dml的核心方法，本类中的{@code insert()}和{@code delete()}方法都将调用该方法执行dml操作。<br/>
     * 该方法将statement，即sql映射位置传给{@code parseStatement()}方法，以便得到JDBC标准slq语句。<br/>
     * 该方法主要将xml文件中的原生sql语句再一次解析得到占位符"#{}"中的字段名，
     * 通过这些字段名来得知该对JDBC的占位符?赋上obj中的哪些字段值。<br/>
     * <p/>
     * 注意，因为在为占位符"?"赋值时，使用的均是{@code setString()}方法，故不能传入一个null作为值。<br/>
     *
     * @param sqlId      sql语句的一个映射，即sql语句的位置。
     * @param parameters 需要更新的对象
     * @return int 返回受影响行数
     */
    @Override
    public int update(String sqlId, Object parameters) throws SQLException {
        // 开启连接资源
        openConnection();
        // 设置SQL处理器
        setSqlHandler(parameters);

        // 根据全限定id，即statement获取对应的SQL映射对象
        MetaMapperStatement metaMapperStatement = statementMap.get(sqlId);
        // 获取对应的原生SQL语句
        String prototypeSql = metaMapperStatement.getPrototypeSql();

        /*封装数据*/
        ForPreparedStatement forPreparedStatement = new ForPreparedStatement(connection, prototypeSql);

        /*获取可以立即执行的preparedStatement实例,
          try-with-resources自动关闭资源
         */
        try (PreparedStatement preparedStatement =
                     sqlHandler.sqlHandler(forPreparedStatement, parameters)) {
            int rowCount = preparedStatement.executeUpdate();
            logger.info("记录更新成功");
            // 执行sql语句, 并返回受影响行数
            return rowCount;
        }
    }

    /**
     * 用于查询parameters对象记录，返回查询得到的对象。
     * <p/>
     * 将自动开启连接。<br/>
     * 当查询无果时返回null。<br/>
     * 当查询结果数量大于1时，抛出异常。<br/>
     * 当且仅当查询结果为1个时，返回查询对象。<br/>
     *
     * @param sqlId      sql语句的一个映射，即sql语句的位置。
     * @param parameters 需要查询的对象
     * @param <T>        泛型，用于限定查询结果的元素类型
     * @return T 指定元素类型的对象
     * @throws SQLException 直接向上抛出
     */
    @Override
    public <T> T selectOne(String sqlId, Object parameters) throws SQLException {
        // 接收结果集合
        List<Object> objects = selectList(sqlId, parameters);
        if (objects.size() == 0) {
            logger.info("查询记录不存在");
            // 如果为0直接返回null
            return null;
        }
        else if (objects.size() > 1) {
            logger.info("查询记录不唯一");
            throw new RuntimeException("查询记录不唯一");
        }
        T t = (T) objects.get(0);
        logger.info("查询唯一记录成功");
        return t;
    }


    /**
     * 用于查询parameters对象记录，返回查询得到的所有对象。<br/>
     * <p/>
     * 将自动开启连接。<br/>
     * @param sqlId      SQL语句的全限定id
     * @param parameters 查询的对象
     * @param <E>        泛型，用于限制集合中元素类型
     * @return {@link List} 存放了结果集记录数据的集合
     * @throws SQLException 直接向上抛出
     */
    @Override
    public <E> List<E> selectList(String sqlId, Object parameters) throws SQLException {
        // 获取SQL映射对象
        MetaMapperStatement metaMapperStatement = statementMap.get(sqlId);
        // 获取SQL返回值类型
        String resultType = metaMapperStatement.getResultType();

        try {
            // 创建对应返回值类型的Class对象
            Class<?> aClass = Class.forName(resultType);
            return selectList(sqlId, parameters, new ListResultHandler<>(aClass));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("创建class对象失败\n" + e.getMessage());
        }

    }

    /**
     * 用于查询parameters对象记录，返回查询得到的所有对象.<br/>
     * <p/>
     * 将自动开启连接。<br/>
     * @param sqlId         SQL语句的全限定id
     * @param parameters    查询的对象
     * @param resultHandler 结果集处理器，其中利用通配符<?>声明是为了可以传入任意的结果集处理器
     * @param <E>           泛型，用于限制集合中元素类型
     * @return {@link List} 存放了结果集记录数据的集合
     * @throws SQLException 直接向上抛出
     */
    public <E> List<E> selectList(String sqlId, Object parameters, ResultHandler<?> resultHandler) throws SQLException {
        // 开启连接
        openConnection();
        // 设置SQL处理器
        setSqlHandler(parameters);

        // 获取SQL映射对象
        MetaMapperStatement metaMapperStatement = statementMap.get(sqlId);
        // 获取原始SQL
        String prototypeSql = metaMapperStatement.getPrototypeSql();

        /*封装数据*/
        ForPreparedStatement forPreparedStatement = new ForPreparedStatement(connection, prototypeSql);

        // 获取preparedStatement实例，并自动关闭
        try (PreparedStatement preparedStatement =
                     sqlHandler.sqlHandler(forPreparedStatement, parameters)) {
            // 获取结果集
            ResultSet resultSet = preparedStatement.executeQuery();
            List<E> res = (List<E>) resultHandler.handler(resultSet);
            logger.info("查询指定记录成功");
            return res;
        }

    }

    /**
     * 公开方法，需要调用本类的方法时，都应该先确保打开连接资源。<br/>
     *
     * @throws SQLException 直接向上抛出异常不做处理
     */
    public void openConnection() throws SQLException {

        if (connection == null) {
            connection = transaction.getConnection();
            logger.info("开启连接成功");
        }
        logger.info("连接已存在");
    }

    /**
     * 设置sql处理程序
     * 用于设置SQL处理器，当传入的单参为Map类型及其子类时，使用{@link MapSqlHandler}，
     * 否则使用{@link ObjectSqlHandler}.<br/>
     * <p/>
     * 该方法应当在每次进行CRUD操作前被调用。<br/>
     *
     * @param arg 传入的单实参
     */
    private void setSqlHandler(Object arg) {
        // 如果传入的参数类型为Map则设置Map类型的SQL处理器，否则使用Object类型
        this.sqlHandler =
                arg instanceof Map ? new MapSqlHandler() : new ObjectSqlHandler();
        logger.info("SQL处理器设置成功");
    }

    private static final Logger logger = ChildLogger.getLogger();

}
