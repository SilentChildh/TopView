package com.child.dao;

import com.child.pojo.UserPO;
import com.child.util.orm.Param;

import java.sql.SQLException;
import java.util.List;

/**
 * 对用户表进行数据库操作。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/12
 */
public interface UserDAO {
    /**
     * 插入一条记录，返回受影响行数
     *
     * @param userPO 指定用户
     * @return 受影响行数
     * @throws SQLException sqlexception异常，直接向上抛出
     */
    int insert(UserPO userPO) throws SQLException;

    /**
     * 根据用户id删除用户数据。
     *
     * @param id 指定id
     * @return 受影响行数
     * @throws SQLException 向上抛出
     */
    int deleteById(@Param("id") Long id) throws SQLException;

    /**
     * 根据用户id更新用户数据。
     *
     * @param id     指定id
     * @param name   指定名字
     * @param oldCar 更新旧汽车品牌
     * @return int 受影响行数
     * @throws SQLException sqlexception异常，向上抛出
     */
    int updateById(@Param("id") Long id, @Param("name") String name, @Param("oldCar") String oldCar) throws SQLException;

    /**
     * 根据用户name查找用户数据。
     *
     * @param name 指定名字
     * @return {@link List}<{@link E}> 返回包含指定返回值类型元素的集合
     * @throws SQLException sqlexception异常，向上抛出
     */
    <E> List<E> selectByName(@Param("name") String name) throws SQLException;

    /**
     * 根据用户id查询用户数据。
     *
     * @param id 唯一的id
     * @return {@link T} 指定返回值类型的实例
     * @throws SQLException sqlexception异常，向上抛出
     */
    <T> T selectById(@Param("id") Long id) throws SQLException;

    /**
     * 根据用户的旧汽车查询用户数据
     *
     * @param oldCar 旧汽车
     * @return {@link List}<{@link E}> 返回包含指定返回值类型元素的集合
     * @throws SQLException sqlexception异常，向上抛出
     */
    <E> List<E> selectByOldCar(@Param("oldCar") String oldCar) throws SQLException;
}
