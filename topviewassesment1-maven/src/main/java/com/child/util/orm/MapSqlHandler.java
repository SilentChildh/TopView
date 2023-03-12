package com.child.util.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * SQL语句处理器，适用于传入的参数列表最终化为一个Map集合的场景。<br/>
 * <p/>
 *
 * @author silent_child
 * @version 1.0
 **/
public class MapSqlHandler implements SqlHandler<Map<String, Object>> {


    /**
     * sql处理程序
     * 传入连接资源、sql语句以及含有特定数据的Map实例来为操作数据库数据进行准备，
     * 调用该方法将返回一个可以立即执行的{@link PreparedStatement}实例。<br/>
     * <p/>
     * 该方法将调用{@code parsePrototypeSql()}方法获取一个解析后的SQL，
     * 再通过该SQL与连接资源和Object实例资源配合进行操作。<br/>
     *
     * @param parameters           含有特定数据，即为占位符"?"传值的数据。K为占位符"#{}"中的字面量值，即属性名，V为要传入的实参值
     * @param forPreparedStatement 包含了创建所需要的参数
     * @return {@link PreparedStatement}
     * @throws SQLException 直接向上抛出
     */
    @Override
    public PreparedStatement sqlHandler(ForPreparedStatement forPreparedStatement,
                                        Map<String, Object> parameters) throws SQLException {
        /*获取所需参数*/
        String prototypeSql = forPreparedStatement.getPrototypeSql();
        Connection connection = forPreparedStatement.getConnection();

        /*获取解析后的sql语句*/
        // 调用接口的默认方法
        String sql = this.parsePrototypeSql(prototypeSql);

        /*接下来解析原生sql中相关占位符中的对象属性信息保存在Map集合中*/
        // 调用接口的默认方法
        Map<Integer, String> field = this.fieldMap(prototypeSql);

        /*将解析好的sql结合先前解析得到的Map集合为占位符"?"进行赋值*/
        // 创建preparedStatement实例
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        // 遍历，通过两个Map集合为每一个占位"?"进行赋值
        field.forEach((index, fieldName) -> {
            try {
                // 得到需要传入的值
                Object obj = parameters.get(fieldName);
                // 给sql语句中的占位符?赋值
                preparedStatement.setObject(index, obj);

            } catch (SQLException e) {
                throw new RuntimeException("赋值失败\n" + e.getMessage());
            }
        });

        return preparedStatement;
    }

}

