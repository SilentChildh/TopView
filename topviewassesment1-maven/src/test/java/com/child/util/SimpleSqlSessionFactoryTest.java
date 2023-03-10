package com.child.util;

import com.child.util.orm.MapperStatement;
import com.child.util.orm.SimpleSqlSessionFactory;
import com.child.util.orm.SqlSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class SimpleSqlSessionFactoryTest {
    private static final DataSource DATASOURCE = ChildDataSource.creatDataSource("default-config");
    private static final Logger LOGGER = ChildLogger.getLogger();
    private static final Map<String, MapperStatement> mapper = new HashMap<>();

    /**
     * 测试构造器
     */
    @Test
    void testConstructor() {
        SimpleSqlSessionFactory simpleSqlSessionFactory =
                new SimpleSqlSessionFactory(DATASOURCE, mapper);
        Class<?> clazz = SimpleSqlSessionFactory.class;
        Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);// 设置为可访问
            try {
                // 打印信息
                LOGGER.info(field.getName() + ":" + field.get(simpleSqlSessionFactory));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

    }

    /**
     * 测试获取会话。<br/>
     * <p/>
     * 首先，创建两个线程，分别获取会话，打开的是不同的会话。<br/>
     * 然后，同一线程中，再次获取会话，打开的还是用一个会话。<br/>
     *
     */
    @Test
    void testOpenSession() {
        SimpleSqlSessionFactory simpleSqlSessionFactory = new SimpleSqlSessionFactory(DATASOURCE, mapper);
        AtomicReference<SqlSession> sqlSession1 = new AtomicReference<>();
        AtomicReference<SqlSession> sqlSession11 = new AtomicReference<>();

        AtomicReference<SqlSession> sqlSession2 = new AtomicReference<>();
        AtomicReference<SqlSession> sqlSession22 = new AtomicReference<>();
        /*通过两个线程开启不同的会话*/
        new Thread(() -> {
            sqlSession1.set(simpleSqlSessionFactory.openSession());
            sqlSession11.set(simpleSqlSessionFactory.openSession());

        }).start();
        new Thread(() ->{
            sqlSession2.set(simpleSqlSessionFactory.openSession());
            sqlSession22.set(simpleSqlSessionFactory.openSession());
        }).start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 断言不同线程得到的是不同会话
        Assertions.assertNotSame(sqlSession1.get(), sqlSession2.get());

        // 断言同一线程得到的是同一会话
        Assertions.assertSame(sqlSession1.get(), sqlSession11.get());
        Assertions.assertSame(sqlSession2.get(), sqlSession22.get());
    }

}
