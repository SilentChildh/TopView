package com.child.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Logger;


public class ChildDataSourceTest {
    private Logger logger = ChildLogger.getLogger();
    @Test
    void testCreateDataSource() {
        DataSource dataSource1 = ChildDataSource.creatDataSource("default-config");
        DataSource dataSource2 = ChildDataSource.creatDataSource("db_01");
        Class<?> aClass = ChildDataSource.class;
        Arrays.stream(aClass.getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        logger.info(field.getName() + "\ndataSource1:" + field.get(dataSource1) +
                                "\ndataSource2:" + field.get(dataSource2));

                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * 设置20个线程，每个线程进行测试：<br/>
     * 在不关闭资源情况下获取到的是同一资源；<br/>
     * <p/>
     * 测试结果：能够满足预期值。<br/>
     */
    @Test
    void testGetConnection1() {
        DataSource dataSource = ChildDataSource.creatDataSource("default-config");
        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                // 测试同一资源
                Connection expected = null;// 期望值，未关闭资源前所获取的所有连接都是同一个
                try {
                    expected = dataSource.getConnection();
                    Connection actual = dataSource.getConnection();// 实际值，再次获取一个资源
                    Assertions.assertSame(expected, actual);// 下断言，每个资源都是同一个
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }).start();// 开启线程
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置20个线程，每个线程进行测试：<br/>
     * 先获取一个资源记录资源地址，然后关闭资源，再次获取一个资源。此时获取到的是不同资源。<br/>
     * <p/>
     * 测试结果：在非公平锁且池中资源量小的情况下，可能会出现拿到同一资源。<br/>
     */
    @Test
    void testGetConnection2() {
        DataSource dataSource = ChildDataSource.creatDataSource("default-config");
        for (int i = 0; i < 20; i++) {
            new Thread(() -> {
                // 测试同一资源
                Connection expected = null;// 期望值，未关闭资源前所获取的所有连接都是同一个
                try {
                    expected = dataSource.getConnection();

                    expected.close();

                    Connection actual = dataSource.getConnection();// 实际值，再次获取一个资源
                    Assertions.assertNotSame(expected, actual);// 下断言，资源不是同一个
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }).start();// 开启线程
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
