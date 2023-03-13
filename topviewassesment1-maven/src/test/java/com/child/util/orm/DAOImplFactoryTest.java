package com.child.util.orm;

import com.child.dao.UserDAO;
import com.child.dao.UserDAOImpl;
import com.child.pojo.UserPO;
import com.child.util.ChildLogger;
import com.child.util.orm.util.DAOImplFactory;
import com.child.util.orm.util.SimpleSqlSessionUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

class DAOImplFactoryTest {
    private final Logger logger = ChildLogger.getLogger();

    @Test
    void getDAOImplProxy() throws SQLException {
        // 执行查询
        UserDAO daoImplProxy = new DAOImplFactory().getDAOImplProxy(UserDAOImpl.class);
        List<Object> list = daoImplProxy.selectByName("李四");
        // 断言以及打印日志
        Assertions.assertNotNull(list);
        logger.info(list.toString());
    }

    @Test
    void testInsertAndUpdate() throws SQLException {
        // 用于操作事务
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();
        // 执行操作
        UserDAO daoImplProxy = new DAOImplFactory().getDAOImplProxy(UserDAOImpl.class);
        UserPO userPO = new UserPO(null, "张三", "@qq.com", "CN", "GTR");
        int insert = daoImplProxy.insert(userPO);
        int i = daoImplProxy.updateById(246L, "樱花", "马车");
        // 断言
        Assertions.assertNotEquals(0, insert);
        // 提交以及关闭
        sqlSession.commit();
        sqlSession.close();
    }
}