package com.child.dao;

import com.child.pojo.UserPO;
import com.child.util.ChildLogger;
import com.child.util.orm.SqlSession;
import com.child.util.orm.SimpleSqlSessionUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UserDaoTest {
    private static final Logger logger = ChildLogger.getLogger();
    @Test
    void testInsert() {

        UserPO userPO = new UserPO();
        //userPO.setId();
        userPO.setName("李四");
        userPO.setEmail("@qq.com");
        userPO.setAddress("北京");
        userPO.setOldCar("宝马");

        UserDAO userDAO = new UserDAOImpl();
        SqlSession sqlSession = null;
        try {
            sqlSession = SimpleSqlSessionUtil.openSession("default-config");
            sqlSession.openConnection();
            Assertions.assertNotEquals(0, userDAO.insert(userPO));
            sqlSession.commit();
        } catch (SQLException e) {
            try {
                sqlSession.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException("添加记录失败" + e);
        }
        finally {
            try {
                sqlSession.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Test
    void testDeleteById() {

        UserDAO userDAO = new UserDAOImpl();
        SqlSession sqlSession = null;
        try {
            sqlSession = SimpleSqlSessionUtil.openSession();
            sqlSession.openConnection();
            Assertions.assertNotEquals(0, userDAO.deleteById(225L));
            sqlSession.commit();
        } catch (SQLException e) {
            try {
                sqlSession.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException("添加记录失败" + e);
        }
        finally {
            try {
                sqlSession.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Test
    void testUpdateById() {

        UserDAO userDAO = new UserDAOImpl();
        SqlSession sqlSession = null;
        try {
            sqlSession = SimpleSqlSessionUtil.openSession();
            sqlSession.openConnection();
            Assertions.assertNotEquals(0,
                    userDAO.updateById(226L, "567", "GTR"));
            sqlSession.commit();
        } catch (SQLException e) {
            try {
                sqlSession.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException("添加记录失败" + e);
        }
        finally {
            try {
                sqlSession.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Test
    void testSelectByNameWithString() {
        UserDAOImpl userDAO = new UserDAOImpl();
        SqlSession sqlSession = null;
        try {
            sqlSession = SimpleSqlSessionUtil.openSession();
            sqlSession.openConnection();
            List<Object> list = userDAO.selectByName("张三");
            Assertions.assertNotNull(list);
            logger.info(list.toString());

            sqlSession.commit();
        } catch (SQLException e) {
            try {
                sqlSession.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException("添加记录失败" + e);
        }
        finally {
            try {
                sqlSession.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void testSelectById() {
        UserDAOImpl userDAO = new UserDAOImpl();
        SqlSession sqlSession = null;
        try {
            sqlSession = SimpleSqlSessionUtil.openSession();
            sqlSession.openConnection();
            Object obj = userDAO.selectById(235L);
            Assertions.assertNotNull(obj);
            logger.info(obj.toString());

            sqlSession.commit();
        } catch (SQLException e) {
            try {
                sqlSession.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException("添加记录失败" + e);
        }
        finally {
            try {
                sqlSession.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void testSelectByOldCar() {
        UserDAOImpl userDAO = new UserDAOImpl();
        SqlSession sqlSession = null;
        try {
            sqlSession = SimpleSqlSessionUtil.openSession();
            sqlSession.openConnection();
            Object obj = userDAO.selectByOldCar("GTR");
            Assertions.assertNotNull(obj);
            logger.info(obj.toString());

            sqlSession.commit();
        } catch (SQLException e) {
            try {
                sqlSession.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException("添加记录失败" + e);
        }
        finally {
            try {
                sqlSession.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
