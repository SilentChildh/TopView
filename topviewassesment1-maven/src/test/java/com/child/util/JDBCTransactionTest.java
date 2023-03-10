package com.child.util;

import com.child.util.orm.JDBCTransaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Logger;

public class JDBCTransactionTest {
    private final Logger LOGGER = ChildLogger.getLogger();
    private final DataSource DATASOURCE = ChildDataSource.creatDataSource("default-config");

    /**
     * 测试两个参数的构造器，通过反射打印连接池对象中的各个属性。<br/>
     */
    @Test
    void testSingleParamConstructor() {
        JDBCTransaction jdbcTransaction = new JDBCTransaction(DATASOURCE);
        Class<?> clazz = JDBCTransaction.class;

        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);// 设置为可访问
            try {
                // 遍历打印属性
                LOGGER.info(field.getName() + ":" + field.get(jdbcTransaction));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        });
    }

    /**
     * 测试两个参数的构造器，通过反射打印连接池对象中的各个属性。<br/>
     */
    @Test
    void testDoubleParamConstructor() {
        JDBCTransaction jdbcTransaction = new JDBCTransaction(DATASOURCE,true);
        Class<?> clazz = JDBCTransaction.class;

        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);// 设置为可访问
            try {
                // 遍历打印属性
                LOGGER.info(field.getName() + ":" + field.get(jdbcTransaction));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        });
    }

    /**
     * 测试{@link JDBCTransaction#isAutoCommit()}方法，分别测试自动提交和手动提交的方式。<br/>
     */
    @Test
    void testIsAutoCommit() {
        /*测试手动提交*/
        JDBCTransaction jdbcTransaction = new JDBCTransaction(DATASOURCE);
        boolean condition = jdbcTransaction.isAutoCommit();// 实际状态
        Assertions.assertFalse(condition);// 断言为false

        /*测试自动提交*/
        jdbcTransaction = new JDBCTransaction(DATASOURCE, true);
        condition = jdbcTransaction.isAutoCommit();// 实际状态
        Assertions.assertTrue(condition);// 断言为真
    }

    /**
     * 测试{@link JDBCTransaction#setAutoCommit(boolean)}方法，分别测试自动提交和手动提交的方式。<br/>
     */
    @Test
    void testSetAutoCommit() {
        /*测试手动提交*/
        // 首先创建一个自动提交的事务管理器
        JDBCTransaction jdbcTransaction = new JDBCTransaction(DATASOURCE, true);
        boolean condition = jdbcTransaction.isAutoCommit();// 实际状态
        Assertions.assertTrue(condition);// 断言为真
        // 然后设置为手动提交
        jdbcTransaction.setAutoCommit(false);
        condition = jdbcTransaction.isAutoCommit();// 实际状态
        Assertions.assertFalse(condition);// 断言为假

        /*测试自动提交*/
        // 首先创建一个手动提交的事务管理器
        jdbcTransaction = new JDBCTransaction(DATASOURCE, false);
        condition = jdbcTransaction.isAutoCommit();// 实际状态
        Assertions.assertFalse(condition);// 断言为假
        // 然后设置为自动提交
        jdbcTransaction.setAutoCommit(true);
        condition = jdbcTransaction.isAutoCommit();// 实际状态
        Assertions.assertTrue(condition);// 断言为真
    }

    /**
     * 测试{@link JDBCTransaction#openConnection()}方法，首先测试一开始为null，开启后不为空。<br/>
     */
    @Test
     void testOpenConnection() {
        JDBCTransaction jdbcTransaction = new JDBCTransaction(DATASOURCE);
        Class<?> clazz = JDBCTransaction.class;
        try {
            Field connection = clazz.getDeclaredField("connection");// 获取connection属性
            connection.setAccessible(true);// 设置为可访问
            Connection actual = (Connection) connection.get(jdbcTransaction);// 获取实际值
            Assertions.assertNull(actual);// 断言此时为空

            Method openConnection = clazz.getDeclaredMethod("openConnection");// 获取openConnection方法
            openConnection.setAccessible(true);// 设置为可访问
            openConnection.invoke(jdbcTransaction);// 调用openConnection方法

            connection = clazz.getDeclaredField("connection");// 获取connection属性
            connection.setAccessible(true);// 设置为可访问
            actual = (Connection) connection.get(jdbcTransaction);// 获取实际值
            Assertions.assertNotNull(actual);// 断言此时不为空
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试获取连接。
     */
    @Test
    void testGetConnection() {
        JDBCTransaction jdbcTransaction = new JDBCTransaction(DATASOURCE);
        try {
            Connection connection = jdbcTransaction.getConnection();// 获取连接
            Assertions.assertNotNull(connection);// 断言不为空
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 测试关闭连接。首先获取资源，此时判定未关闭。然后关闭资源，再判定已关闭。<br/>
     */
    @Test
    void testClose() {
        JDBCTransaction jdbcTransaction = new JDBCTransaction(DATASOURCE);
        try {
            /*测试未关闭*/
            Connection connection = jdbcTransaction.getConnection();// 获取资源
            boolean condition = connection.isClosed();// 实际状态
            Assertions.assertFalse(condition);// 断言为false，即未关闭

            /*测试已关闭*/
            jdbcTransaction.close();// 关闭资源
            condition = connection.isClosed();// 实际状态
            Assertions.assertTrue(condition);// 断言为true，即已关闭
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试提交事务方式，该测试需要连接数据库，并准备表以及字段进行测试。<br/>
     * <p/>
     * 首先测试手动提交，然后测试自动提交。<br/>
     * 需要注意的是，在通过{@link JDBCTransaction#setAutoCommit(boolean)}之后，
     * 需要重新获取全新连接资源，将提交事务的方式作用于新的资源上。<br/>
     */
    @Test
    void testCommit() {
        JDBCTransaction jdbcTransaction = new JDBCTransaction(DATASOURCE, false);
        Connection connection;
        try {
            /*测试手动提交， 执行更新后还需提交，否则数据库记录不会更新*/
            connection = jdbcTransaction.getConnection();// 获取连接资源
            String sql = "insert into t_user (name) values (?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "李四");

            Assertions.assertEquals(1, preparedStatement.executeUpdate());// 执行更新
            jdbcTransaction.commit();// 手动提交

            /*测试自动提交， 执行更新后数据库中立马添加记录*/
            jdbcTransaction.setAutoCommit(true);// 重新设置提交方式
            connection = jdbcTransaction.getConnection();// 获取新的提交方式的连接资源
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "王五");
            Assertions.assertEquals(1, preparedStatement.executeUpdate());// 执行更新，自动提交


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试事务回滚操作。，该测试需要连接数据库，并准备表以及字段进行测试。<br/>
     * <p/>
     * 首先测试不回滚，再测试回滚操作。<br/>
     */
    @Test
    void testRollback() {
        JDBCTransaction jdbcTransaction = new JDBCTransaction(DATASOURCE, false);
        // 获取连接
        Connection connection = null;
        try {
            connection = jdbcTransaction.getConnection();
            String sql = "update user_money set balance=balance-100 where name=('王五')";
            String sql2 = "update user_money set balance=balance+100 where name=('李四')";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            // 断言更新成功
            Assertions.assertEquals(1, preparedStatement.executeUpdate());
            int i = 1/ 0;
            preparedStatement = connection.prepareStatement(sql2);
            // 断言更新成功
            Assertions.assertEquals(1, preparedStatement.executeUpdate());
            // 执行成功则提交
            jdbcTransaction.commit();




        } catch (Exception e) {
            String sql = "select * from user_money";
            PreparedStatement preparedStatement;
            /*查询出现异常后sql的执行情况*/
            try {
                preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();
                LOGGER.info("出现异常后sql的执行情况");
                while (resultSet.next()) {
                    LOGGER.info(resultSet.getString("name") +
                            resultSet.getString("balance"));
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            try {
                // 执行回滚
                jdbcTransaction.rollback();
                /*查询回滚事务后sql的执行情况*/
                preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery();
                LOGGER.info("回滚事务后sql的执行情况");
                while (resultSet.next()) {
                    LOGGER.info(resultSet.getString("name") +
                            resultSet.getString("balance"));
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            LOGGER.info("已处理事务异常");
        }
    }
}
