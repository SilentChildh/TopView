package com.child.util.orm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * SQL语句处理器，适用于传入的实参为pojo类的场景。<br/>
 * <p/>
 * @author silent_child
 * @version 1.0
 **/

public class ObjectSqlHandler implements SqlHandler<Object>{

    /**
     * 传入连接资源、sql语句以及含有特定数据的pojo实例来为操作数据库数据进行准备，
     * 调用该方法将返回一个可以立即执行的{@link PreparedStatement}实例。<br/>
     * <p/>
     * 该方法将调用{@code parsePrototypeSql()}方法获取一个解析后的SQL，
     * 再通过该SQL与连接资源和Object实例资源配合进行操作。<br/>
     *
     * @param connection   指定的连接资源，用于创建实例
     * @param prototypeSql 写在xml文件中的原生SQL
     * @param parameters   含有特定数据，即为占位符"?"传值的数据
     * @return {@link PreparedStatement}
     * @throws SQLException 直接向上抛出
     */
    @Override
    public PreparedStatement sqlHandler(Connection connection, String prototypeSql,
                                        Object parameters) throws SQLException {
        // 获取parameters的运行类型
        Class<?> parametersClass = parameters.getClass();

        /*接下来开始解析原生sql为符合JDBC规范的sql语句*/
        // 调用接口的默认方法
        String sql = this.parsePrototypeSql(prototypeSql);

        /*接下来解析原生sql中相关占位符中的对象属性信息保存在Map集合中*/
        // 调用接口的默认方法
        Map<Integer, String> field = this.fieldMap(prototypeSql);


        /*将解析好的sql结合先前解析得到的Map集合为占位符"?"进行赋值*/
        // 用于拼接po类获取字段值的方法名
        final String get = "get";
        // 创建preparedStatement实例
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        // 遍历，通过反射为每一个占位"?"进行赋值
        field.forEach((index, fieldName) -> {
            // 每一个占位符中的字段的get方法名
            String getMethodName = get + (char) (fieldName.charAt(0) - 32) + fieldName.substring(1);
            try {
                // 通过反射获取get方法
                Method getMethod = parametersClass.getDeclaredMethod(getMethodName);
                // 调用get方法得到obj中的私有属性，然后给sql语句中的占位符?赋值
                Object invoke = getMethod.invoke(parameters);
                preparedStatement.setObject(index, invoke);

            } catch (SQLException e) {
                throw new RuntimeException("赋值失败\n" + e.getMessage());
            }
            catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("反射调用方法出错\n" + e.getMessage());
            }
        });

        // 返回赋完值的preparedStatement实例
        return preparedStatement;
    }

}
