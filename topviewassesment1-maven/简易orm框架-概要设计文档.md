## DataBasePool

~~~java
package com.child.util;

import com.child.exception.MinIdledException;
import com.child.exception.OverMaxActiveException;
import com.child.exception.TimedOutException;

import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * 用于获取连接池中空闲的连接。<br/>
 * <p/>
 * 关于字段：<br/>
 * 数据库连接池，利用池化技术来避免多次与数据库的物理连接与关闭，进而提升性能，并优化用户体验。<br/>
 * static与final结合，实现底层自动优化，即在类加载的准备阶段就会被赋上指定的字面量值。<br/>
 * 资源绑定器{@code ThreadLocal}，保证了一个线程在未释放资源前，每次获取到的连接都是同一个。<br/>
 * <p/>
 * 关于方法：<br/>
 * 核心方法 {@code getConnection()}用于返回连接资源，其中会保证多线程同步，并将经过三个自定义异常检测，
 * 分别是{@link MinIdledException}, {@link OverMaxActiveException}, {@link TimedOutException}。<br/>
 *
 * 重要方法一 {@code reclaim()}用于回收资源入池，并解除线程绑定资源。<br/>
 * 重要方法二 {@code getConnectionProxy()}用于获取连接的代理类，其中特别强调了在调用close方法时，
 * 调用的是本类中的{@code reclaim()}方法。<br/>
 *
 * @author silent_child
 * @version 1.0
 **/

public class DataBasePool {
    private static final Logger logger = ChildLogger.getLogger();// 日志
    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("dataBasePool-config");
    private static final String driver = resourceBundle.getString("driver");
    private static final String url = resourceBundle.getString("url");
    private static final String user = resourceBundle.getString("username");
    private static final String pwd = resourceBundle.getString("password");
    /**
     * 初始化连接数
     */
    private static final int initialSize = Integer.parseInt(resourceBundle.getString("initialSize"));
    /**
     * 空闲池中最少连接资源数量，默认为5
     */
    private static final int minIdle = Integer.parseInt(resourceBundle.getString("minIdle"));
    /**
     * 活跃状态下的最大连接数，默认为20
     */
    private static final int maxActive = Integer.parseInt(resourceBundle.getString("maxActive"));
    /**
     * 最大连接时长，默认是5s
     */
    private static final int maxWait = Integer.parseInt(resourceBundle.getString("maxWait"));
    /**
     * 用于为线程绑定连接资源，使得每一个线程在未释放资源时，获取的都是同一个资源。
     */
    private static final ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

    /**
     * 使用并发队列，线程安全，而且是队列数据结构，更符合池化技术
     */
    private static final ConcurrentLinkedQueue<Connection> queue = new ConcurrentLinkedQueue<>();
    /**
     * 目前处于活跃状态的连接资源数量，默认为0。<br/>
     * 将作为{@link DataBasePool#reclaim(Connection)}方法中的写锁，故使用包装类。<br/>
     * <p/>
     * 注意：static只是保证该字段在内存中的唯一性，但不能保证可见性。
     * volatile只能保证可见性但不能保证原子性，故后续若有多线程操作还需要通过上锁来保证原子性<br/>
     */
    private static volatile Integer presentMaxActive = 0;

    /**
     * 记录获取资源释放是否超时，默认为false
     */
    private static boolean isTimedOut;

    static {
        // 注册数据库驱动

        // 获取初始化资源
    }

    /**
     * 用于从空闲池中获取连接资源。
     */
    public static Connection getConnection() {
    }

    /**
     * 用于获取连接的代理类。要求在调用close()方法时，调用的时本类的reclaim()方法
     */
    private static Connection getConnectionProxy() {
    }

    /**
     * 用于回收资源到空闲池
     */
    public static Void reclaim(Connection connection) {
    }

    /**
     * 用于判断空闲连接数量
     */
    private static void isMinIdled() {
    }

    /**
     * 用于判断活跃连接数量
     */
    private static void isOverMaxActive() {
    }

    /**
     * 用于判断获取连接是否超时
     */
    private static void isTimedOut() {
    }

    // 私有化构造器，防止外界创建DataBasePool对象
    private DataBasePool() {
    }
}
~~~


