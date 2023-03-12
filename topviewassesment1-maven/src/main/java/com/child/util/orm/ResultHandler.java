package com.child.util.orm;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 结果集处理器，在对数据库进行查询操作时，可以通过不同的策略而选择不同的结果集处理器。<br/>
 * 泛型T用于限制结果集被{@code handler()}处理后的返回类型
 * @author silent_child
 * @version 1.0
 **/

public interface ResultHandler<T> {
    /**
     * 核心方法，用于处理结果集返回一个封装好的对象。例如JavaBean、List<T>等.
     *
     * @param resultSet 结果集
     * @return {@link T} 返回包含结果集的对象
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    T handler(ResultSet resultSet) throws SQLException;

}
