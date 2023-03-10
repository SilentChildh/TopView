package com.child.util.orm;

import java.sql.SQLException;
import java.util.List;

/**
 * SqlSession实例的顶级接口，声明了每个SqlSession实例都应该拥有的方法。<br/>
 * <p/>
 * 需要注意的是，对于提交事务相关的异常直接向上抛出，不在内部进行额外的处理。<br/>
 * 并且每一个SqlSession实例的作用范围应该是请求域，即局部作用域。<br/>
 * @author silent_child
 * @version 1.0
 **/

public interface SqlSession extends AutoCloseable {
    /**
     * 用于关闭会话。
     */
    void close() throws SQLException;

    /**
     * 提交事务。
     */
    void commit() throws SQLException;

    /**
     * 回滚事务。
     */
    void rollback() throws SQLException;

    /**
     * 用于插入parameters对象记录，返回受影响行数。
     */
    int insert(String sqlId, Object parameters) throws SQLException;

    /**
     * 用于删除parameters对象记录，返回受影响行数。
     */
    int delete(String sqlId, Object parameters) throws SQLException;

    /**
     * 用于更新parameters对象记录，返回受影响行数。
     */
    int update(String sqlId, Object parameters) throws SQLException;

    /**
     * 用于插入parameters对象记录，返回查询得到的对象。
     */
    <T> T selectOne(String sqlId, Object parameters) throws SQLException;

    /**
     * 用于插入parameters对象记录，返回查询得到的所有对象。
     */
    <E> List<E> selectList(String sqlId, Object parameters) throws SQLException;
}
