package com.child.dao;

import com.child.pojo.UserPO;
import com.child.util.orm.SqlSession;
import com.child.util.orm.SimpleSqlSessionUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UserDAOImpl implements UserDAO{
    @Override
    public int insert(UserPO userPO) throws SQLException {
        // 通过工具类获取获取会话
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession("default-config");
        // 执行sql操作，返回受影响行数
        return sqlSession.insert("com.child.dao.UserDAO.insert", userPO);
    }
    public int insert(Map<String, Object> map) throws SQLException {
        // 通过工具类获取获取会话
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession("default-config");
        // 执行sql操作，返回受影响行数
        return sqlSession.insert("com.child.dao.UserDAO.insert", map);
    }

    @Override
    public int deleteById(UserPO userPo) throws SQLException {
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        return sqlSession.delete("com.child.dao.UserDAO.deleteById", userPo);
    }

    @Override
    public int updateById(UserPO userPo) throws SQLException {
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        return sqlSession.update("com.child.dao.UserDAO.updateById", userPo);
    }

    @Override
    public <E> List<E> selectByName(UserPO userPO) throws SQLException {
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        return sqlSession.selectList("com.child.dao.UserDAO.selectByName", userPO);
    }
    public <T> T selectById(UserPO userPO) throws SQLException {
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        return sqlSession.selectOne("com.child.dao.UserDAO.selectById", userPO);
    }

    public <T> T selectByName(String name) throws SQLException {
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        return sqlSession.selectOne("com.child.dao.UserDAO.selectByName", name);
    }
    public <T> T selectByName(Map<String, Object> map) throws SQLException {
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        return sqlSession.selectOne("com.child.dao.UserDAO.selectByName", map);
    }

}
