package com.child.util.orm;

import javax.sql.DataSource;
import java.util.Map;


/**
 * 会话工厂的实现类，用于开启一段会话<br/>
 * <p/>
 * 工厂类根据配置文件的不同而不同，但应存在唯一性，即一个配置文件对应一个工厂类。
 * 这意味着工厂类应该作为应用域对象。<br/>
 * @author silent_child
 * @version 1.0
 **/

public class SimpleSqlSessionFactory implements SqlSessionFactory{
    /**
     * 每一个SimpleSqlSessionFactory实例都将持有一个<strong>唯一</strong>的数据库连接池对象。<br/>
     * 对于每一个SimpleSqlSession实例都将获得该数据库连接池的访问权限。
     * （但实际上会话类是通过事务管理器来获取数据库连接池的访问权限的）<br/>
     */
    private final DataSource dataSource;
    /**
     * 每一个SimpleSqlSessionFactory实例都将持有一个<strong>唯一</strong>的SQL映射集合。<br/>
     * 对于每一个SimpleSqlSession实例都将获得该SQL映射集合的可读权限。
     */
    private final Map<String, MapperStatement> statementMap;
    /**
     * 对每一个线程将绑定一个SqlSession实现类。
     * 同一线程仅能持有一个会话类。
     */
    private final ThreadLocal<SqlSession> threadLocal = new ThreadLocal<>();

    /**
     * 用于创建一个SimpleSqlSessionFactory实例，
     * 每一个SimpleSqlSessionFactory实例都将持有从配置文件中读取到的数据库连接池和SQL映射集合。
     * @param dataSource 一个事务管理器
     * @param statementMap 一个承载SQL映射语句的集合
     */
    public SimpleSqlSessionFactory(DataSource dataSource, Map<String, MapperStatement> statementMap) {
        this.dataSource = dataSource;
        this.statementMap = statementMap;
    }

    /**
     * 开启会话并返回一个会话类{@code SimpleSqlSession}，默认手动提交事务。
     * @return {@link SqlSession}
     */
    @Override
    public SqlSession openSession() {
        return openSession(false);
    }

    /**
     * 返回一个SqlSession的实现类{@code SimpleSqlSession}，
     * 每个会话实现类都将拥有工厂类关于{@code dataSource}和{@code statementMapper}的访问权限。<br/>
     * <p/>
     * @param autoCommit 提交事务的方式，true为自动提交，false为手动提交。
     * @return {@link SqlSession} 实际上是{@link SimpleSqlSession}，是接口的实现类。
     */
    @Override
    public SqlSession openSession(boolean autoCommit) {

        // 从线程资源绑定器中尝试获取会话资源
        SqlSession sqlSession = threadLocal.get();
        // 如果存在会话资源，则直接返回
        if (sqlSession != null) return sqlSession;
        // 创建一个全新的事务管理器
        Transaction transaction = new JDBCTransaction(dataSource, autoCommit);

        // 创建会话类，直接将工厂类中的事务管理器和SQL映射集合传入即可。
        sqlSession = new SimpleSqlSession(transaction, statementMap);
        threadLocal.set(sqlSession);// 会话资源与线程绑定

        return sqlSession;// 最后返回会话资源
    }

}
