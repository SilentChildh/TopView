package com.child.util.orm;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * SQL语句处理器的顶级接口，用于声明具体处理器中必要的方法.<br/>
 * <p/>
 * 除此之外，还提供了一个default方法，该方法可以视作一个工具方法，用于解析原生SQL语句。<br/>
 * @author silent_child
 * @version 1.0
 **/

public interface SqlHandler<T> {
    /**
     * 核心方法，用于处理SQL语句，并将{@code parameters}中的数据赋值给PreparedStatement实例，最后返回该实例。<br/>
     *
     * @param parameters           带有具体数据的实例
     * @param forPreparedStatement 包含了创建所需要的参数
     * @return {@link PreparedStatement}
     * @throws SQLException 直接向上抛出
     */
    PreparedStatement sqlHandler(ForPreparedStatement forPreparedStatement, T parameters) throws SQLException;


    /**
     * 将xml文件中的SQL语句转换为符合JDBC规范以及符合数据库表字段名规范的SQL语句。<br/>
     * <p/>
     * 该方法首先会利用正则表达式将xml文件中的原生sql语句进行占位符的替换，即将"#{}"替换为"?"。<br/>
     * 并另字段名从java规范命名替换为sql规范命名。<br/>
     *
     * @param prototypeSql 原生SQL语句
     * @return String 返回一个符合JDBC规范的sql语句
     */
    default String parsePrototypeSql(String prototypeSql) {
        // 正则表达式，用于将整个占位符"#{}"的所有内容替换为"?"
        String tempSql = prototypeSql.replaceAll("#\\{[a-zA-Z0-9_$]*}", "?");
        // 对字符串进行操作
        StringBuilder sql = new StringBuilder(tempSql);
        // 以空格、逗号、括号分割
        String[] words = prototypeSql.split("[\\s,()]");

        for (String word : words) {
            for (int j = 0; j < word.length() - 1; j++) {
                // 如果前后两个字母大小写不一致，则进行替换
                if (Character.isLowerCase(word.charAt(j)) && Character.isUpperCase(word.charAt(j + 1))) {

                    // 替换之前的大写字母为 "_"和小写字母
                    String replace = "_" + Character.toLowerCase(word.charAt(j + 1));
                    StringBuilder newWord = new StringBuilder(word);
                    newWord.replace(j + 1, j + 2, replace);

                    // 找到原sql中该单词的位置
                    int wordIndex = sql.indexOf(word);
                    if (wordIndex == -1) {
                        break;
                    }

                    // 对原SQL进行替换
                    sql.replace(wordIndex, wordIndex + word.length(), newWord.toString());

                }
            }
        }

        return sql.toString();
    }

    /**
     * 通过原生SQL语句，得到占位符"#{}"出现的次序和其中的字面量值，并一一对应的放入Map中。<br/>
     * @param prototypeSql 原生SQL语句
     * @return {@link Map}
     */
    default Map<Integer, String> fieldMap(String prototypeSql) {
        // 存放占位符"#{}"中查询得到的次序和属性名
        // noinspection AlibabaCollectionInitShouldAssignCapacity
        Map<Integer, String> field = new HashMap<>();
        // 占位符的左半边
        final String left = "#{";
        // 占位符的右半边
        final String right = "}";
        // 子串的起始位置
        int begin;
        // 子串的最终位置
        int end;
        // 开始查询字符串的位置
        int fromIndex = 0;
        // 记录占位符出现的次序
        int i = 1;
        while (true) {
            // 找到"#{"字符串后的第一个字符索引
            begin = prototypeSql.indexOf(left, fromIndex) + 2;
            // 找到"}"字符的索引
            end = prototypeSql.indexOf(right, fromIndex);
            // 未找到则退出
            if (end <= 0) {
                break;
            }

            // 截取begin和end直接的字符串
            field.put(i++, prototypeSql.substring(begin, end));
            // 将开始查询的位置设置为"}"字符后的第一个字符索引
            fromIndex = end + 1;
        }
        return field;
    }


}
