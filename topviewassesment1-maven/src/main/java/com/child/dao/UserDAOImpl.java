package com.child.dao;

import com.child.pojo.UserPO;
import com.child.util.orm.ParametersHandler;
import com.child.util.orm.SqlSession;
import com.child.util.orm.SimpleSqlSessionUtil;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * {@link UserDAO}的实现类，用于对数据库中用户表进行操作。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/12
 */
public class UserDAOImpl implements UserDAO{
    public static final Class<UserDAO> USERDAO_CLASS = UserDAO.class;
    @Override
    public int insert(UserPO userPO) throws SQLException {
        // 通过工具类获取获取会话
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession("default-config");

        // 创建一个参数处理器，并得到处理后的单参数
        ParametersHandler insert = new ParametersHandler("insert", USERDAO_CLASS, new Object[]{userPO});
        Object handle = insert.handle();

        // 将单参数传入，执行sql操作，返回受影响行数
        return sqlSession.insert("com.child.dao.UserDAO.insert", handle);
    }

    @Override
    public int deleteById(Long id) throws SQLException {
        // 通过工具类获取获取会话
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        // 创建一个参数处理器，并得到处理后的单参数
        ParametersHandler insert =
                new ParametersHandler("deleteById", USERDAO_CLASS,
                        new Object[]{id});
        Object handle = insert.handle();

        // 将单参数传入，执行sql操作，返回受影响行数
        return sqlSession.delete("com.child.dao.UserDAO.deleteById", handle);
    }

    @Override
    public int updateById(Long id, String name, String oldCar) throws SQLException {
        // 通过工具类获取获取会话
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        // 创建一个参数处理器，并得到处理后的单参数
        ParametersHandler insert =
                new ParametersHandler("updateById", USERDAO_CLASS,
                        new Object[]{id, name, oldCar});
        Object handle = insert.handle();

        // 将单参数传入，执行sql操作，返回受影响行数
        return sqlSession.update("com.child.dao.UserDAO.updateById", handle);
    }

    @Override
    public <E> List<E> selectByName(String name) throws SQLException {
        // 通过工具类获取获取会话
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        // 创建一个参数处理器，并得到处理后的单参数
        ParametersHandler insert =
                new ParametersHandler("selectByName", USERDAO_CLASS,
                        new Object[]{name});
        Object handle = insert.handle();

        // 将单参数传入，执行sql操作，返回受影响行数
        return sqlSession.selectList("com.child.dao.UserDAO.selectByName", handle);
    }


    @Override
    public <T> T selectById(Long id) throws SQLException {
        // 通过工具类获取获取会话
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        // 创建一个参数处理器，并得到处理后的单参数
        ParametersHandler insert =
                new ParametersHandler("selectById", USERDAO_CLASS,
                        new Object[]{id});
        Object handle = insert.handle();

        // 将单参数传入，执行sql操作，返回受影响行数
        return sqlSession.selectOne("com.child.dao.UserDAO.selectById", handle);
    }

    @Override
    public <E> List<E> selectByOldCar(String oldCar) throws SQLException {
        // 通过工具类获取获取会话
        SqlSession sqlSession = SimpleSqlSessionUtil.openSession();

        // 创建一个参数处理器，并得到处理后的单参数
        ParametersHandler insert =
                new ParametersHandler("selectByOldCar", USERDAO_CLASS,
                        new Object[]{oldCar});
        Object handle = insert.handle();

        // 将单参数传入，执行sql操作，返回受影响行数
        return sqlSession.selectList("com.child.dao.UserDAO.selectByOldCar", handle);
    }
}
