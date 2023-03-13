package com.child.util.orm.bean;

import com.child.util.orm.handler.SqlHandler;

import java.sql.Connection;

/**
 * 用于{@link SqlHandler#sqlHandler(ForPreparedStatement, Object)}，
 * 将多参数封装到一个实体类中，利于维护。<br/>
 */
public class ForPreparedStatement {
    /**
     * 指定数据源的连接资源
     */
    private Connection connection;

    /**
     * 指定映射文件中的SQL
     */
    private String prototypeSql;

    public ForPreparedStatement(Connection connection, String prototypeSql) {
        this.connection = connection;
        this.prototypeSql = prototypeSql;
    }
    public Connection getConnection() {
        return connection;
    }

    public String getPrototypeSql() {
        return prototypeSql;
    }
}
