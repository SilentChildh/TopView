package com.child.util.orm;

import com.child.util.ChildDataSource;
import com.child.util.ChildLogger;
import com.child.util.orm.bean.ForPreparedStatement;
import com.child.util.orm.handler.MapSqlHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


class MapSqlHandlerTest {
    public static final Logger logger = ChildLogger.getLogger();

    @Test
    void sqlHandler() {
        /*设置所需配置*/
        ChildDataSource childDataSource = ChildDataSource.creatDataSource("default-config");
        Connection connection = childDataSource.getConnection();
        String prototypeSql = "select * from t_user where name = #{name} AND oldCar = #{oldCar}";
        ForPreparedStatement forPreparedStatement = new ForPreparedStatement(connection, prototypeSql);

        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("oldCar", "马自达");

        /*创建SQL处理器, 并进行处理*/
        MapSqlHandler mapSqlHandler = new MapSqlHandler();
        try {
            PreparedStatement preparedStatement = mapSqlHandler.sqlHandler(forPreparedStatement, map);
            Assertions.assertNotNull(preparedStatement);
            logger.info(preparedStatement.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}