package com.child.util.orm;

/**
 * 作为每一个SqlSessionFactory实例的顶级接口，声明了每一个实例都应该拥有的方法。<br/>
 * <p/>
 * 注意每一个SqlSessionFactory实例的作用域应该是应用域范围。<br/>
 * @author silent_child
 * @version 1.0
 **/

public interface SqlSessionFactory {
    /**
     * 开启会话。默认手动提交事务
     * @return {@link SqlSession}
     */
    SqlSession openSession();

    /**
     * 开启会话，设置提交事务方式
     * @param autoCommit 提交事务的方式，true为自动提交，false为手动提交。
     * @return {@link SqlSession}
     */
    SqlSession openSession(boolean autoCommit);

}
