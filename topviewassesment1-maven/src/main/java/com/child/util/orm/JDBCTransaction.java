package com.child.util.orm;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 事务管理器，用于管理事务相关操作。<br/>
 * <p/>
 * 注意，每一个{@link SimpleSqlSession}都应该持有一个全新的事务管理器。<br/>
 * 事务管理器的作用域也应该是请求域范围。<br/>
 * 由于父类接口继承了{@link AutoCloseable}，故可以通过try-with-resource进行自动关闭连接资源。
 * @author silent_child
 * @version 1.0
 **/

public class JDBCTransaction implements Transaction {
    /**
     * 数据库连接池。<br/>
     * 每一个事务管理器将持有对应Factory类中对数据库连接池的引用。
     */
    private final DataSource dataSource;
    /**
     * 连接资源。<br/>
     * 每一个事务管理器都将从数据库连接池中获取一份全新连接资源，且仅能持有一份。<br/>
     */
    private Connection connection;
    /**
     * 是否自动提交事务，默认为false，即手动提交。
     */
    private boolean autoCommit;

    /**
     * 根据数据库连接池创建一个事务管理器。默认手动提交事务。<br/>
     * <p/>
     * @param dataSource Factory类中数据库连接池对象的引用
     */
    public JDBCTransaction(DataSource dataSource) {
        this(dataSource, false);
    }

    /**
     * 根据数据库连接池和指定提交事务方法来创建一个事务管理器.<br/>
     * <p/>
     * @param dataSource Factory类中数据库连接池对象的引用
     * @param autoCommit 提交事务的方式，false为手动提交，true为自动提交。
     */
    public JDBCTransaction(DataSource dataSource, boolean autoCommit) {
        this.dataSource = dataSource;
        this.autoCommit = autoCommit;
    }

    /**
     * 返回一个全新的连接资源。
     * @return {@link Connection}
     */
    @Override
    public Connection getConnection() throws SQLException {
        openConnection();// 开启连接
        return connection;
    }

    /**
     * 开启一个全新的连接资源。此后可以直接通过{@code ChildTransaction}实例得到连接资源.<br/>
     * <p/>
     * @throws SQLException 直接向上抛出异常，不作处理
     */
    private void openConnection() throws SQLException {
        connection = dataSource.getConnection();// 获取全新连接资源
        connection.setAutoCommit(autoCommit);// 设置连接资源的状态
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    @Override
    public void commit() throws SQLException {
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException {
        connection.rollback();
    }


    /**
     * 判断事务提交的方式。<br/>
     * <p/>
     * 需要主要的是，您获取到的信息可能并不是你所拥有的connection的状态信息。<br/>
     * 原因是你可能在获取连接资源之后，又调用了{@code setAutoCommit()}方法。
     * 而该方法实际上无法作用到已开启的连接资源中。<br/>
     * @return boolean true时为自动提交
     */
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * 设置提交事务的方式。需要注意的是，需要在获取资源前调用此方法，否则无效。<br/>
     * @param autoCommit 提交事务的方式，true时为自动提交，false为手动提交
     */
    public void setAutoCommit(boolean autoCommit){
        this.autoCommit = autoCommit;
    }
}
