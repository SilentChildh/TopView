package com.child.util.orm;

import com.child.util.ChildDataSource;
import com.child.util.ChildLogger;
import com.child.util.orm.bean.MetaMapperStatement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

class SimpleSqlSessionFactoryTest {
    private static final DataSource DATASOURCE = ChildDataSource.creatDataSource("default-config");
    private static final Logger LOGGER = ChildLogger.getLogger();
    private static final Map<String, MetaMapperStatement> mapper = new HashMap<>();

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
     */
    @Test
    void testOpenSession() {
        SimpleSqlSessionFactory simpleSqlSessionFactory = new SimpleSqlSessionFactory(DATASOURCE, mapper);

        SqlSession sqlSession1 = simpleSqlSessionFactory.openSession();
        SqlSession sqlSession2 = simpleSqlSessionFactory.openSession();

        // 断言同一线程得到的会话不为null
        Assertions.assertNotNull(sqlSession1);
        Assertions.assertNotNull(sqlSession2);
        // 断言同一线程得到的是不同会话
        Assertions.assertNotSame(sqlSession1, sqlSession2);
    }


}