package com.child.util.orm;

import java.sql.SQLException;
import java.util.List;

/**
 * SqlSession实例的顶级接口，声明了每个SqlSession实例都应该拥有的方法。<br/>
 * <p/>
 * 需要注意的是，对于提交事务相关的异常直接向上抛出，不在内部进行额外的处理。<br/>
 * 并且每一个SqlSession实例的作用范围应该是请求域，即局部作用域。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/12
 */

public interface SqlSession extends AutoCloseable {
    /**
     * 用于关闭会话。
     *
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    void close() throws SQLException;

    /**
     * 提交事务。
     *
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    void commit() throws SQLException;

    /**
     * 回滚事务。
     *
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    void rollback() throws SQLException;

    /**
     * 用于插入parameters对象记录，返回受影响行数。
     *
     * @param sqlId      sql id
     * @param parameters 参数
     * @return int 返回受影响行数。
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    int insert(String sqlId, Object parameters) throws SQLException;

    /**
     * 用于删除parameters对象记录，返回受影响行数。
     *
     * @param sqlId      sql id
     * @param parameters 参数
     * @return int 返回受影响行数。
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    int delete(String sqlId, Object parameters) throws SQLException;

    /**
     * 用于更新parameters对象记录，返回受影响行数。
     *
     * @param sqlId      sql id
     * @param parameters 参数
     * @return int 返回受影响行数。
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    int update(String sqlId, Object parameters) throws SQLException;

    /**
     * 用于插入parameters对象记录，返回查询得到的对象。
     *
     * @param sqlId      sql id
     * @param parameters 参数
     * @return {@link T} 指定返回值类型的对象
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    <T> T selectOne(String sqlId, Object parameters) throws SQLException;

    /**
     * 用于插入parameters对象记录，返回查询得到的所有对象。
     *
     * @param sqlId      sql id
     * @param parameters 参数
     * @return {@link List}<{@link E}> 返回包含指定返回值类型的元素的集合
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    <E> List<E> selectList(String sqlId, Object parameters) throws SQLException;

    /**
     * 打开连接。每次对数据库进行操作前，都应该打开连接资源。<br/>
     *
     * @throws SQLException sqlexception异常
     */
    void openConnection() throws SQLException;
}
