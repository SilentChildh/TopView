package com.child.dao;

import com.child.pojo.UserPO;

import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    /**
     * 插入一条纪律，返回受影响行数
     * @return 受影响行数
     */
    int insert(UserPO userPO) throws SQLException;

    /**
     * 根据用户id删除用户数据。
     * @param userPo 指定用户
     * @return 受影响行数
     * @throws SQLException 向上抛出
     */
    int deleteById(UserPO userPo) throws SQLException;
    int updateById(UserPO userPo) throws SQLException;

    <E> List<E> selectByName(UserPO userPO) throws SQLException;
    <T> T selectById(UserPO userPO) throws SQLException;

}
