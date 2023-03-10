package com.child.util.orm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;

import java.util.*;

/**
 * 该sql会话主要用于对数据库进行一系列的操作。集成了操作数据库必要的功能。<br/>
 * 其中{@code close()},{@code commit()},{@code rollback()}方法主要在service层被调用。<bt/>
 * 其中有关CRUD的操作均在dao层被调用。<br/>
 * 该类中的异常均向上抛出，交由调用者进行处理。<br/>
 * SqlSession的作用域应该是局部的，即用完就应该销毁。<br/>
 *
 * @author silent_child
 * @version 1.0
 **/
public class SimpleSqlSession implements SqlSession {
    /**
     * 每一个SqlSession实例都将持有一个<strong>唯一</strong>的事务管理器，用于管理事务相关的操作。
     */
    private final Transaction transaction;

    /**
     * 从transaction实例中获取到的连接资源，对于每一个会话类，应该仅持有<strong>唯一</strong>的一份连接资源，
     * 并通过该连接资源完成整个会话关于数据库的操作，而不应该频繁的通过transaction对连接资源进行获取或者关闭。
     */
    private Connection connection;
    /**
     * 每一个sqlSession实例将拥有{@link SimpleSqlSessionFactory}类中所有SQL语句映射的访问权限。
     * K为sql语句全限定id，V为包含标签信息的对象
     */
    private final Map<String, MapperStatement> statementMap;

    /**
     * 创建一个SqlSession对象，可以进行都数据库的操作。
     *
     * @param transaction  事务管理器
     * @param statementMap 包含SQL映射语句的集合
     */
    public SimpleSqlSession(Transaction transaction, Map<String, MapperStatement> statementMap) {
        this.transaction = transaction;
        this.statementMap = statementMap;
    }

    /**
     * 获取一个动态代理的SqlSession实例{@link SimpleSqlSessionProxy}。
     * @return {@link SimpleSqlSession}
     */
    public SimpleSqlSession createSimpleSqlSession() {
        Class<SimpleSqlSession> simpleSqlSessionClass = SimpleSqlSession.class;

        return (SimpleSqlSession) Proxy.newProxyInstance(simpleSqlSessionClass.getClassLoader(),
                simpleSqlSessionClass.getInterfaces(), new SimpleSqlSessionProxy());
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
        openConnection();// 开启连接资源
        // 根据全限定id，即statement获取对应的SQL映射对象
        MapperStatement mapperStatement = statementMap.get(sqlId);
        // 获取对应的原生SQL语句
        String prototypeSql = mapperStatement.getPrototypeSql();

        // 获取可以立即执行的preparedStatement实例
        // try-with-resources自动关闭资源
        try (PreparedStatement preparedStatement = parameters instanceof Map ?
                SimpleSqlSessionUtil.sqlHandler(connection, prototypeSql, (Map<String, Object>) parameters) :
                SimpleSqlSessionUtil.sqlHandler(connection, prototypeSql, parameters)) {

            return preparedStatement.executeUpdate();// 执行sql语句, 并返回受影响行数
        }
    }

    /**
     * 用于查询parameters对象记录，返回查询得到的对象。
     * <p/>
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
        List<Object> objects = selectList(sqlId, parameters);// 接收结果集合
        if (objects.size() == 0) return null;// 如果为0直接返回null
        else if (objects.size() > 1) throw new RuntimeException("查询记录不唯一");

        return (T) objects.get(0);
    }


    /**
     * 用于查询parameters对象记录，返回查询得到的所有对象。<br/>
     * <p/>
     *
     * @param sqlId      SQL语句的全限定id
     * @param parameters 查询的对象
     * @param <E>        泛型，用于限制集合中元素类型
     * @return {@link List} 存放了结果集记录数据的集合
     * @throws SQLException 直接向上抛出
     */
    @Override
    public <E> List<E> selectList(String sqlId, Object parameters) throws SQLException {
        MapperStatement mapperStatement = statementMap.get(sqlId);// 获取SQL映射对象
        String resultType = mapperStatement.getResultType();// 获取SQL返回值类型

        try {
            Class<?> aClass = Class.forName(resultType);// 创建对应返回值类型的Class对象
            return selectList(sqlId, parameters, new ListResultHandler<>(aClass));// 调用
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 用于查询parameters对象记录，返回查询得到的所有对象.<br/>
     * <p/>
     *
     * @param sqlId         SQL语句的全限定id
     * @param parameters    查询的对象
     * @param resultHandler 结果集处理器，其中利用通配符<?>声明是为了可以传入任意的结果集处理器
     * @param <E>           泛型，用于限制集合中元素类型
     * @return {@link List} 存放了结果集记录数据的集合
     * @throws SQLException 直接向上抛出
     */
    public <E> List<E> selectList(String sqlId, Object parameters, ResultHandler<?> resultHandler) throws SQLException {
        openConnection();// 开启连接

        MapperStatement mapperStatement = statementMap.get(sqlId);// 获取SQL映射对象
        String prototypeSql = mapperStatement.getPrototypeSql();// 获取原始SQL

        // 获取preparedStatement实例，并自动关闭
        try (PreparedStatement preparedStatement = parameters instanceof Map ?
                SimpleSqlSessionUtil.sqlHandler(connection, prototypeSql, (Map<String, Object>) parameters) :
                SimpleSqlSessionUtil.sqlHandler(connection, prototypeSql, parameters)) {
            ResultSet resultSet = preparedStatement.executeQuery();// 获取结果集

            return (List<E>) resultHandler.handler(resultSet);
        }

    }

    /**
     * 私有方法，用于本类确保在对数据库进行操作时，都能打开连接资源。<br/>
     *
     * @throws SQLException 直接向上抛出异常不做处理
     */
    private void openConnection() throws SQLException {
        if (connection == null) {
            connection = transaction.getConnection();
        }
    }

    class SimpleSqlSessionProxy implements InvocationHandler {
        /**
         * 被代理的会话类
         */
        private final SimpleSqlSession simpleSqlSession = new SimpleSqlSession(transaction, statementMap);
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if
            return null;
        }
    }
}
