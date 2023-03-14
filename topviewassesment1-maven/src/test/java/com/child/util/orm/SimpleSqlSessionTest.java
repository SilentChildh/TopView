package com.child.util.orm;

import com.child.pojo.UserPO;
import com.child.util.ChildDataSource;
import com.child.util.ChildLogger;
import com.child.util.orm.bean.MetaMapperStatement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SimpleSqlSessionTest {
    public static final DataSource DATASOURCE = ChildDataSource.creatDataSource("default-config");
    public static final Transaction TRANSACTION = new JdbcTransaction(DATASOURCE);
    public static final Transaction TRANSACTION_AUTO_COMMIT = new JdbcTransaction(DATASOURCE, true);
    public static final Map<String, MetaMapperStatement> mapper = new HashMap<>();
    public static final Logger LOGGER = ChildLogger.getLogger();
    /**
     * 测试构造器。
     */
    @Test
    void testConstructor() {
        SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper);
        Class<?> clazz = SimpleSqlSession.class;
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);// 设置为可访问
            try {
                // 遍历打印信息
                LOGGER.info(field.getName() + ":" + field.get(simpleSqlSession));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 测试开启连接资源。<br/>
     * <p/>
     * 首先测试连接未打开，即为null时打开新连接。<br/>
     * 然后再测试连接已打开，即不为null时，连接没有变化.<br/>
     */
    @Test
    void testOpenConnection() {
        SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper);
        Class<? extends SimpleSqlSession> aClass = simpleSqlSession.getClass();
        try {
            // 获取字段
            Field connection = aClass.getDeclaredField("connection");
            // 设置访问权限
            connection.setAccessible(true);
            // 获取对应会话类中的connection值
            Object actual = connection.get(simpleSqlSession);
            // 断言此时为null
            Assertions.assertNull(actual);


            // 获取方法
            Method openConnection = aClass.getDeclaredMethod("openConnection");
            // 设置访问权限
            openConnection.setAccessible(true);
            // 调用open方法
            openConnection.invoke(simpleSqlSession);
            // 更新actual
            actual = connection.get(simpleSqlSession);
            // 断言此时不为空
            Assertions.assertNotNull(actual);


            Object excepted = actual;// 将预期值指向之前打开的连接
            // 再次调用open方法，并试图得到新的连接
            openConnection.invoke(simpleSqlSession);
            actual = connection.get(simpleSqlSession);// 实际值指向“新连接”
            // 断言连接还是同一个
            Assertions.assertSame(excepted, actual);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试关闭方法.<br/>
     * <p/>
     * 首先测试未关闭，然后测试关闭。<br/>
     */
    @Test
    void testClose() {
        SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper);
        Class<SimpleSqlSession> simpleSqlSessionClass = SimpleSqlSession.class;
        try {
            // 获取连接字段
            Field connection = simpleSqlSessionClass.getDeclaredField("connection");
            connection.setAccessible(true);// 设置为可访问
            // 获取open方法
            Method openConnection = simpleSqlSessionClass.getDeclaredMethod("openConnection");
            openConnection.setAccessible(true);// 设置为可访问
            // 打开连接
            openConnection.invoke(simpleSqlSession);

            // 获取连接值
            Connection actual = (Connection) connection.get(simpleSqlSession);

            // 断言此时处于连接未关闭
            Assertions.assertFalse(actual.isClosed());
            // 接下来关闭连接
            simpleSqlSession.close();
            // 断言此时连接处于关闭状态
            Assertions.assertTrue(actual.isClosed());

        } catch (SQLException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试提交事务。<br/>
     * <p/>
     * 需要借助表字段以及另一个线程。首先测试手动提交，执行更新后，另一线程查询结果并未立即更新，手动提交后，查询结果更新。<br/>
     * 然后测试自动提交，执行更新后，另一线程查询结果立即更新。<br/>
     */
    @Test
    void testCommit() {
        SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper);
        SimpleSqlSession autoCommitSimpleSqlSession = new SimpleSqlSession(TRANSACTION_AUTO_COMMIT, mapper);
        Class<SimpleSqlSession> simpleSqlSessionClass = SimpleSqlSession.class;
        /*测试手动提交*/
        try {
            // 获取连接字段
            Field connection = simpleSqlSessionClass.getDeclaredField("connection");
            connection.setAccessible(true);// 设置为可访问
            // 获取open方法
            Method openConnection = simpleSqlSessionClass.getDeclaredMethod("openConnection");
            openConnection.setAccessible(true);// 设置为可访问
            // 打开连接
            openConnection.invoke(simpleSqlSession);
            // 获取连接值
            Connection actual = (Connection) connection.get(simpleSqlSession);

            String updateSql = "insert into user_money (name, balance) values ('嘤嘤嘤', '100')";
            String querySql = "select * from user_money";
            // 断言执行成功
            Assertions.assertEquals(1, actual.prepareStatement(updateSql).executeUpdate());
            // 开启线程打印查询结果
            new Thread(() -> {
                LOGGER.info("手动提交前：");
                ResultSet resultSet = null;
                try {
                    resultSet = actual.prepareStatement(querySql).executeQuery();
                    while (resultSet.next()) {
                        LOGGER.info(resultSet.getString("name") + "\t" +
                                resultSet.getString("balance"));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).start();


            // 手动提交
            simpleSqlSession.commit();

            new Thread(() -> {
                // 再次查询，并打印
                LOGGER.info("手动提交后：");
                ResultSet resultSet = null;
                try {
                    resultSet = actual.prepareStatement(querySql).executeQuery();
                    while (resultSet.next()) {
                        LOGGER.info(resultSet.getString("name") + "\t" +
                                resultSet.getString("balance"));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).start();

        } catch (NoSuchFieldException | InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                 SQLException e) {
            throw new RuntimeException(e);
        }


        /*测试自动提交*/
        try {
            // 获取连接字段
            Field connection = simpleSqlSessionClass.getDeclaredField("connection");
            connection.setAccessible(true);// 设置为可访问
            // 获取open方法
            Method openConnection = simpleSqlSessionClass.getDeclaredMethod("openConnection");
            openConnection.setAccessible(true);// 设置为可访问
            // 打开连接
            openConnection.invoke(autoCommitSimpleSqlSession);
            // 获取连接值
            Connection actual = (Connection) connection.get(autoCommitSimpleSqlSession);

            String updateSql = "insert into user_money (name, balance) values ('嘤嘤嘤', '100')";
            String querySql = "select * from user_money";
            // 断言执行成功
            Assertions.assertEquals(1, actual.prepareStatement(updateSql).executeUpdate());
            // 开启线程打印查询结果
            new Thread(() -> {
                LOGGER.info("自动提交：");
                ResultSet resultSet = null;
                try {
                    resultSet = actual.prepareStatement(querySql).executeQuery();
                    while (resultSet.next()) {
                        LOGGER.info(resultSet.getString("name") + "\t" +
                                resultSet.getString("balance"));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).start();

        } catch (NoSuchFieldException | InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                 SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试事务回滚。<br/>
     * <p/>
     * 回滚前，可查询到执行的结果，回滚后，无更新。<br/>
     */
    @Test
    void testRollback() {
        SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper);
        Class<SimpleSqlSession> simpleSqlSessionClass = SimpleSqlSession.class;
        Connection actual = null;// 接收连接实际值
        try {
            // 获取连接字段
            Field connection = simpleSqlSessionClass.getDeclaredField("connection");
            connection.setAccessible(true);// 设置为可访问
            // 获取open方法
            Method openConnection = simpleSqlSessionClass.getDeclaredMethod("openConnection");
            openConnection.setAccessible(true);// 设置为可访问
            // 打开连接
            openConnection.invoke(simpleSqlSession);
            // 获取连接值
            actual = (Connection) connection.get(simpleSqlSession);


            String updateSql = "insert into user_money (name, balance) values ('嘤嘤嘤', '100')";

            // 断言执行成功
            Assertions.assertEquals(1, actual.prepareStatement(updateSql).executeUpdate());
            int i = 1 / 0;
            // 手动提交
            simpleSqlSession.commit();

        } catch (Exception e) {
            String querySql = "select * from user_money";

            LOGGER.info("未回滚前：");
            ResultSet resultSet = null;
            try {
                resultSet = actual.prepareStatement(querySql).executeQuery();
                while (resultSet.next()) {
                    LOGGER.info(resultSet.getString("name") + "\t" +
                            resultSet.getString("balance"));
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            try {
                simpleSqlSession.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            LOGGER.info("回滚后：");
            try {
                resultSet = actual.prepareStatement(querySql).executeQuery();
                while (resultSet.next()) {
                    LOGGER.info(resultSet.getString("name") + "\t" +
                            resultSet.getString("balance"));
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * 测试插入记录。
     */
    @Test
    void testInsert() {
        String sqlId = "com.child.dao.UserDAO.insert";
        String prototypeSql = "insert into t_user (name, email, address) values (#{name}, #{email}, #{address})";
        String sqlType = "insert";
        String resultType = "com.child.pojo.UserPO";
        // 在映射集合中添加一条SQL映射
        MetaMapperStatement metaMapperStatement = new MetaMapperStatement(sqlId, sqlType, prototypeSql, resultType);
        mapper.put(sqlId, metaMapperStatement);
        // 创建一个对象
        UserPO userPO = new UserPO(null, "红花", "@qq.com", "beijing");
        // 创建会话类进行更新

        try (SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper)) {
            // 断言执行成功
            Assertions.assertNotEquals(0, simpleSqlSession.insert(sqlId, userPO));
            simpleSqlSession.commit();// 提交事务
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 测试删除记录。<br/>
     * <p/>
     * 先插入一条记录，然后再进行删除。<br/>
     */
    @Test
    void testDelete() {
        // 插入一条记录
        try (SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper)) {
            String sqlId = "com.child.dao.UserDAO.insert";
            String prototypeSql = "insert into t_user (name, email, address) values (#{name}, #{email}, #{address})";
            String sqlType = "insert";
            String resultType = "com.child.pojo.UserPO";
            // 在映射集合中添加一条SQL映射
            MetaMapperStatement metaMapperStatement = new MetaMapperStatement(sqlId, sqlType, prototypeSql, resultType);
            mapper.put(sqlId, metaMapperStatement);
            // 创建一个对象
            UserPO userPO = new UserPO(null, "红花", "@qq.com", "beijing");
            // 断言执行成功
            Assertions.assertNotEquals(0, simpleSqlSession.insert(sqlId, userPO));
            simpleSqlSession.commit();// 提交事务
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        /*接下来对插入的记录进行删除*/
        String sqlId = "com.child.dao.UserDAO.deleteByName";
        String prototypeSql = "delete from t_user where name = #{name}";
        String sqlType = "delete";
        String resultType = "com.child.pojo.UserPO";
        // 在映射集合中添加一条SQL映射
        MetaMapperStatement metaMapperStatement = new MetaMapperStatement(sqlId, sqlType, prototypeSql, resultType);
        mapper.put(sqlId, metaMapperStatement);
        // 创建一个对象
        UserPO userPO = new UserPO(null, "张三", null, null);
        // 创建会话类进行更新

        try (SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper)) {
            // 断言执行成功
            Assertions.assertNotEquals(0, simpleSqlSession.delete(sqlId, userPO));
            simpleSqlSession.commit();// 提交事务
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 测试更新记录.br/>
     * <p/>
     * 先插入一条记录，然后再进行更新。<br/>
     */
    @Test
    void testUpdate() {
        // 插入一条记录
        try (SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper)) {
            String sqlId = "com.child.dao.UserDAO.insert";
            String prototypeSql = "insert into t_user (name, email, address) values (#{name}, #{email}, #{address})";
            String sqlType = "insert";
            String resultType = "com.child.pojo.UserPO";
            // 在映射集合中添加一条SQL映射
            MetaMapperStatement metaMapperStatement = new MetaMapperStatement(sqlId, sqlType, prototypeSql, resultType);
            mapper.put(sqlId, metaMapperStatement);
            // 创建一个对象
            UserPO userPO = new UserPO(null, "梨花", "@qq.com", "beijing");
            // 断言执行成功
            Assertions.assertNotEquals(0, simpleSqlSession.insert(sqlId, userPO));
            simpleSqlSession.commit();// 提交事务
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        /*接下来进行更新*/
        String sqlId = "com.child.dao.UserDAO.updateByName";
        String prototypeSql = "update t_user set name = '梅花' where name = #{name}";
        String sqlType = "update";
        String resultType = "com.child.pojo.UserPO";
        // 在映射集合中添加一条SQL映射
        MetaMapperStatement metaMapperStatement = new MetaMapperStatement(sqlId, sqlType, prototypeSql, resultType);
        mapper.put(sqlId, metaMapperStatement);
        // 创建一个对象
        UserPO userPO = new UserPO(null, "梨花", null, null);
        // 创建会话类进行更新

        try (SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper)) {
            // 断言执行成功
            Assertions.assertNotEquals(0, simpleSqlSession.update(sqlId, userPO));
            simpleSqlSession.commit();// 提交事务
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 测试查询多条记录。<br/>
     */
    @Test
    void testSelectList() {
        /*模拟Mapper映射*/
        String sqlId = "com.child.dao.UserDAO.selectByName";
        String prototypeSql = "select * from t_user where name = #{name};";
        String sqlType = "select";
        String resultType = "com.child.pojo.UserPO";
        // 在映射集合中添加一条SQL映射
        MetaMapperStatement metaMapperStatement = new MetaMapperStatement(sqlId, sqlType, prototypeSql, resultType);
        mapper.put(sqlId, metaMapperStatement);

        UserPO userPO = new UserPO(null, "李四", null, null, null);
        try (SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper)) {
            List<Object> list = simpleSqlSession.selectList(sqlId, userPO);
            // 断言执行成功
            Assertions.assertNotNull(list);
            LOGGER.info(list.toString());
            simpleSqlSession.commit();// 提交事务
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void testSelectOne() {
        /*模拟Mapper映射*/
        String sqlId = "com.child.dao.UserDAO.selectByName";
        String prototypeSql = "select * from t_user where id = #{id};";
        String sqlType = "select";
        String resultType = "com.child.pojo.UserPO";
        // 在映射集合中添加一条SQL映射
        MetaMapperStatement metaMapperStatement = new MetaMapperStatement(sqlId, sqlType, prototypeSql, resultType);
        mapper.put(sqlId, metaMapperStatement);

        UserPO userPO = new UserPO(222L, null, null, null, null);
        try (SimpleSqlSession simpleSqlSession = new SimpleSqlSession(TRANSACTION, mapper)) {
            Object object = simpleSqlSession.selectOne(sqlId, userPO);
            // 断言执行成功
            Assertions.assertNotNull(object);
            LOGGER.info(object.toString());
            simpleSqlSession.commit();// 提交事务
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
