package com.child.util.orm;

import com.child.util.ChildLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSqlSessionUtilTest {
    public static final String DEFAULT_CONFIG = "default-config";
    public static final Logger LOGGER = ChildLogger.getLogger();

    /**
     * 测试开启会话。<br/>
     * <p/>
     * 首先检测工具类中的Map元素数量为0，调用了{@code openSession()}方法之后元素数量为1.<br/>
     * 然后再调用一次，检测Map集合中不会多出一个工厂类.<br/>
     */
    @Test
    void testOpenSession() {
        Class<SimpleSqlSessionUtil> simpleSqlSessionUtilClass = SimpleSqlSessionUtil.class;
        try {
            // 获取Map字段
            Field sqlSessionFactoryMap =
                    simpleSqlSessionUtilClass.getDeclaredField("SQL_SESSION_FACTORY_MAP");
            sqlSessionFactoryMap.setAccessible(true);// 设置为可访问
            // 获取Map集合
            Map<String, MapperStatement> mapperStatementMap =
                    (Map<String, MapperStatement>) sqlSessionFactoryMap.get(sqlSessionFactoryMap);
            // 断言此时集合元素数量为0
            Assertions.assertEquals(0, mapperStatementMap.size());

            // 调用open方法
            SimpleSqlSessionUtil.openSession(DEFAULT_CONFIG, false);
            // 断言此时Map集合元素数量为1
            Assertions.assertEquals(1, mapperStatementMap.size());

            // 再调用一次open方法
            SimpleSqlSessionUtil.openSession(DEFAULT_CONFIG, false);
            // 断言此时元素数量还是1
            Assertions.assertEquals(1, mapperStatementMap.size());

        } catch (NoSuchFieldException | IllegalAccessException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 测试{@link SimpleSqlSessionUtil#build(String)}方法。<br/>
     * 首先准备一个配置文件，然后通过build方法得到工厂类，再将工厂类的信息进行打印。
     */
    @Test
    void build() {
        SqlSessionFactory factory = SimpleSqlSessionUtil.build(DEFAULT_CONFIG);
        Class<? extends SqlSessionFactory> aClass = factory.getClass();
        Arrays.stream(aClass.getDeclaredFields()).forEach(field -> {
            field.setAccessible(true);// 设置为可访问
            try {
                // 打印相关信息
                LOGGER.info(field.getName() + '\t' + field.get(factory));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }



    /**
     * 测试从文件目录路径中的配置文件中获取SQL映射集合。
     */
    @Test
    void testGetStatementMapperFromDirectory() {
        String directorName =
                "F:\\TopViewAssessment1\\topviewassesment1-maven\\src\\main\\resources\\com\\child\\dao";
        Class<SimpleSqlSessionUtil> simpleSqlSessionUtilClass = SimpleSqlSessionUtil.class;
        try {
            // 获取方法
            Method getMapperFromDirectory =
                    simpleSqlSessionUtilClass.getDeclaredMethod("getStatementMapperFromDirectory", String.class);
            getMapperFromDirectory.setAccessible(true);// 设置为可访问
            Map<String, MapperStatement> invoke =
                    (Map<String, MapperStatement>) getMapperFromDirectory.invoke(null, directorName);
            invoke.entrySet().stream()
                    .forEach(entry -> LOGGER.info(entry.getKey() + "\t" + entry.getValue()));

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试从包名中的配置文件中获取SQL映射集合。
     */
    @Test
    void testGetStatementMapperFromPackage() {
        String packageName = "com.child.dao";
        Map<String, MapperStatement> statementMapperFromPackage =
                SimpleSqlSessionUtil.getStatementMapperFromPackage(packageName);

        statementMapperFromPackage.entrySet().stream()
                .forEach(entry -> LOGGER.info(entry.getKey() + "\t" + entry.getValue()));

    }
}