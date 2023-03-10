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
        try (SqlSession sqlSession = SimpleSqlSessionUtil.openSession("default-config")) {
            System.out.println(userDAO.insert(userPO));
            sqlSession.commit();
        } catch (SQLException e) {
            throw new RuntimeException("添加记录失败" + e);
        }
    }
    @Test
    void testInsertWithMap() {

        Map<String, Object> map = new HashMap<>();

        map.put("name", "梅花");
        map.put("email", "@qq.com");
        map.put("address", "shanghai");
        map.put("oldCar", "奔驰");

        UserDAOImpl userDAO = new UserDAOImpl();
        try (SqlSession sqlSession = SimpleSqlSessionUtil.openSession()) {
            Assertions.assertNotEquals(0, userDAO.insert(map));
            sqlSession.commit();
        } catch (SQLException e) {
            throw new RuntimeException("添加记录失败" + e);
        }
    }
    @Test
    void testDeleteById() {

        UserPO userPO = new UserPO();
        userPO.setId(26L);

        UserDAO userDAO = new UserDAOImpl();
        try (SqlSession sqlSession = SimpleSqlSessionUtil.openSession()) {
            System.out.println(userDAO.deleteById(userPO));
            sqlSession.commit();
        } catch (SQLException e) {
            throw new RuntimeException("添加记录失败" + e);
        }
    }
    @Test
    void testUpdateById() {

        UserPO userPO = new UserPO();
        userPO.setId(25L);
        userPO.setOldCar("特斯拉");

        UserDAO userDAO = new UserDAOImpl();
        try (SqlSession sqlSession = SimpleSqlSessionUtil.openSession()) {
            System.out.println(userDAO.updateById(userPO));
            sqlSession.commit();
        } catch (SQLException e) {
            throw new RuntimeException("添加记录失败" + e);
        }
    }

    @Test
    void testSelectById() {
        UserPO userPO = new UserPO(212L, null, null, null);
        UserDAO userDAO = new UserDAOImpl();
        try (SqlSession sqlSession = SimpleSqlSessionUtil.openSession()) {
            UserPO objects = userDAO.selectById(userPO);
            Assertions.assertNotNull(objects);
            logger.info(objects.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSelectByName() {
        UserPO userPO = new UserPO(null, "张三", null, null);
        UserDAO userDAO = new UserDAOImpl();
        try (SqlSession sqlSession = SimpleSqlSessionUtil.openSession()) {
            List<Object> objects = userDAO.selectByName(userPO);
            Assertions.assertNotNull(objects);
            logger.info(objects.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void testSelectByNameWithString() {
        String name = "李四";
        UserDAOImpl userDAO = new UserDAOImpl();
        try (SqlSession sqlSession = SimpleSqlSessionUtil.openSession()) {
            List<Object> objects = userDAO.selectByName(name);
            Assertions.assertNotNull(objects);
            logger.info(objects.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSelectByNameWithMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "梅花");
        UserDAOImpl userDAO = new UserDAOImpl();
        try (SqlSession sqlSession = SimpleSqlSessionUtil.openSession()) {
            Object objects = userDAO.selectByName(map);
            Assertions.assertNotNull(objects);
            logger.info(objects.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
