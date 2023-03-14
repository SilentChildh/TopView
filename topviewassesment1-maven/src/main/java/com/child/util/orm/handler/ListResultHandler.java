package com.child.util.orm.handler;


import com.child.util.ChildLogger;
import com.child.util.orm.handler.ResultHandler;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 继承了{@link ResultHandler}接口，该类将作为结果集的处理器，
 * 调用方法{@code handler()}后将会另结果集封装在List集合中，并返回List集合。<br/>
 * 泛型E限制了List集合中的元素类型，
 * 为父接口传入泛型参数List<E>用于声明{@code handler()}方法返回的是List<E>。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/12
 */
public class ListResultHandler<E> implements ResultHandler<List<E>>{

    /**
     * 返回值类型，用于确定泛型E的具体类型
     */
    private final Class<?> resultType;
    /**
     * List集合，用于接收封装了结果集数据的E类型的元素。
     */
    private final List<E> list = new ArrayList<>();

    /**
     * 用于创建结果集处理器，调用者需要传入返回值类型，该类型将会是List集合的元素类型
     * @param resultType 返回值类型的类对象
     */
    public ListResultHandler(Class<?> resultType) {
        this.resultType = resultType;
    }

    /**
     * 核心方法，用于将结果集中的记录封装到指定返回值类型的元素中，
     * 并将元素放入List集合中。最后返回一个List集合。<br/>
     * <p/>
     * 该方法主要分三步。第一步，遍历所有记录。第二步，对每一个记录进行处理。第三步，返回List集合。<br/>
     * 接下来重点将第二步的操作。该步骤中，首先需要创建一个实例来接收结果集中一条记录中的数据。
     * 然后开始遍历记录中的每一列信息，通过反射获取实例中的字段，并对其进行赋值。最后将实例放入List集合中。<br/>
     * 重复第二步直到遍历所有记录。
     * @param resultSet 指定结果集
     * @return {@link List}存放着结果集数据的List集合
     * @throws SQLException 直接向上抛出
     */
    @Override
    public List<E> handler(ResultSet resultSet) throws SQLException {
        // 获得结果集元信息
        ResultSetMetaData metaData = resultSet.getMetaData();
        // 获取结果集列数
        int columnCount = metaData.getColumnCount();

        /*接下来遍历所有记录*/
        while (resultSet.next()) {
            // 创建一个实例，用于装载该条记录的信息
            Object object;
            try {
                object = resultType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("创建元素实例失败\n" + e.getMessage());
            }

            // 遍历每个记录中的每一列
            for (int i = 1; i <= columnCount; i++) {
                // 获取该列的字段名
                String columnName = metaData.getColumnName(i);

                /*接下来得到符合驼峰命名的字段名*/
                StringBuilder stringBuilder = new StringBuilder(columnName);
                int end = 0;
                while (true) {
                    end = stringBuilder.indexOf("_");
                    if (end == -1) {
                        break;
                    }
                    stringBuilder.replace(end, end + 2, String.valueOf((char)(stringBuilder.charAt(end + 1) - 32)));
                }
                // 获取符合驼峰命名的字段名
                String filedName = String.valueOf(stringBuilder);

                /*接下来将记录装载到实例中*/
                try {

                    // 获取字段
                    Field field = resultType.getDeclaredField(filedName);
                    // 设置为可访问
                    field.setAccessible(true);

                    // 获取指定字段下的记录值
                    Object value = resultSet.getObject(columnName);

                    // 赋值
                    field.set(object, value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("返回值类型的字段不存在\n" + e.getMessage());
                }

            }
            /*最后将装载记录的Object对象放入List集合中*/
            list.add((E) object);
        }
        logger.info("结果集收集为List集合成功");
        return list;
    }
    private static final Logger logger = ChildLogger.getLogger();

}
