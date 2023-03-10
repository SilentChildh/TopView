package com.child.util;

import com.child.exception.MinIdledException;
import com.child.exception.OverMaxActiveException;
import com.child.exception.TimedOutException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Logger;

/**
 * 创建数据库连接池。用于获取连接池中空闲的连接。<br/>
 * 可以同时创建多个连接池，每个连接池将拥有自己的特性。<br/>
 * <p/>
 * <strong>关于字段：</strong>
 * <ol>
 *     <li>数据库连接池，利用池化技术来避免多次与数据库的物理连接与关闭，进而提升性能，并优化用户体验。</li>
 *     <li>final修饰保证值不可变</li>
 *     <li>
 *          连接池容器{@code QUEUE}选择使用阻塞队列{@link LinkedBlockingQueue}来保证线程安全.
 *          同时需要强调的是，
 *          使用并发队列{@link ConcurrentLinkedQueue}可能会导致{@code size()}方法不是百分百准确，
 *          故本类不使用该队列来存储连接资源。
 *     </li>
 *     <li>资源绑定器{@code THREAD_LOCAL}，保证了一个线程在未释放资源前，每次获取到的连接都是同一个。</li>
 *     <li>累加器{@code PRESENT_MAX_ACTIVE}，保证了多线程操作时对于活跃数量增减的原子性与可见性，即保证了线程安全。</li>
 * </ol>
 * <strong>关于方法：</strong>
 * <ol>
 *     <li>用于获取连接池对象的方法{@code creatDataSource()}。</li>
 *     <li>
 *         核心方法 {@code getConnection()}用于返回连接资源，其中会保证多线程同步，并将经过三个自定义异常检测，
 *         分别是{@link MinIdledException}, {@link OverMaxActiveException}, {@link TimedOutException}。
 *     </li>
 *     <li>重要方法一 {@code release()}用于回收资源入池，并解除线程绑定资源。</li>
 *     <li>
 *         重要方法二 {@code getConnectionProxy()}用于获取连接的代理类，其中特别强调了在调用{@code close()}方法时，
 *         调用的是本类中的{@code release()}方法。
 *     </li>
 * </ol>
 *
 * @author silent_child
 * @version 1.0
 **/

public class ChildDataSource implements DataSource {
    private static final Logger logger = ChildLogger.getLogger();// 日志
    private final String DRIVER;
    private final String URL;
    private final String USER;
    private final String PASSWORD;
    /**
     * 初始化连接数，默认为10
     */
    private final int INITIAL_SIZE;
    /**
     * 空闲池中最少连接资源数量，默认为5
     */
    private final int MIN_IDLE;
    /**
     * 活跃状态下的最大连接数，默认为20
     */
    private final int MAX_ACTIVE;
    /**
     * 最大连接时长，默认是5s
     */
    private final int MAX_WAIT;

    /**
     * 用于为线程绑定连接资源，使得每一个线程在未释放资源时，获取的都是同一个资源。
     */
    private final ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

    /**
     * 使用阻塞队列，线程安全，而且是队列数据结构，更符合池化技术。用于保存空闲状态的连接资源。<br/>
     * 之所以不选择并发队列，是因为size()方法可能并不准确
     */
    private final LinkedBlockingQueue<Connection> idlePool = new LinkedBlockingQueue<>();
    /**
     * 使用并发hashMap，线程安全，用于保存处于活跃状态的连接资源。<br/>
     * 需要注意的是，不应该使用size()方法来统计活跃连接数量，因为它可能是不准确的。<br/>
     * 该池采用延迟初始化，在调用{@code createConnection()}时，根据最大活跃数量配置信息进行初始化。<br/>
     * 且为了防止临界值扩容的情况，初始容量应该设置为 elementSum / loaderFactor + 1.<br/>
     */
    private Map<Connection, ConnectionStatus> activePool;
    /**
     * 累加器，用于记录当前活跃连接数量
     */
    private final LongAdder presentMaxActive = new LongAdder();


    /**
     * 私有化，放置外界通过构造器创建连接池对象。<br/>
     *
     * @param driver      驱动
     * @param url         资源位置
     * @param user        数据库用户名
     * @param password    数据库密码
     * @param initialSize 初始化连接资源数量
     * @param minIdle     连接资源的最小空闲数量
     * @param maxActive   连接资源的最大活跃数量
     * @param maxWait     获取连接资源的最大耗时
     */
    private ChildDataSource(String driver, String url, String user, String password,
                            int initialSize, int minIdle, int maxActive, int maxWait) {
        this.DRIVER = driver;
        this.URL = url;
        this.USER = user;
        this.PASSWORD = password;
        this.INITIAL_SIZE = initialSize;
        this.MIN_IDLE = minIdle;
        this.MAX_ACTIVE = maxActive;
        this.MAX_WAIT = maxWait;
    }

    /**
     * 用于外界获取连接池对象，可以通过该对象获取连接资源。<br/>
     * <p/>
     *
     * @param resource 连接池配置文件类路径
     * @return {@link ChildDataSource} 一个数据库连接池对象
     */
    public static ChildDataSource creatDataSource(String resource) {
        // 将资源文件的相关信息保存在资源包中
        ResourceBundle resourceBundle = ResourceBundle.getBundle(resource);
        // 获取对应资源
        String driver = resourceBundle.getString(ConfigConstants.DRIVER);
        String url = resourceBundle.getString(ConfigConstants.URL);
        String user = resourceBundle.getString(ConfigConstants.USER);
        String password = resourceBundle.getString(ConfigConstants.PASSWORD);
        int initialSize = Integer.parseInt(resourceBundle.getString(ConfigConstants.INITIAL_SIZE));
        int minIdle = Integer.parseInt(resourceBundle.getString(ConfigConstants.MIN_IDLE));
        int maxActive = Integer.parseInt(resourceBundle.getString(ConfigConstants.MAX_ACTIVE));
        int maxWait = Integer.parseInt(resourceBundle.getString(ConfigConstants.MAX_WAIT));
        // 通过构造器创建连接池对象
        ChildDataSource childDataSource =
                new ChildDataSource(driver, url, user, password, initialSize, minIdle, maxActive, maxWait);
        // 根据最大活跃数量对活跃池进行初始化
        childDataSource.activePool = new ConcurrentHashMap<>((int)(maxActive / 0.75) + 1);
        try {
            // 注册数据库驱动
            Class.forName(childDataSource.DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 获取初始化资源
        for (int i = 0; i < childDataSource.INITIAL_SIZE; i++) {
            childDataSource.idlePool.offer(childDataSource.getConnectionProxy());
        }
        logger.info("数据库连接池初始化完毕");
        return childDataSource;
    }

    /**
     * 用于从空闲池中获取连接资源。<br/>
     * <p/>
     * 以{@code queue}为锁，防止多线程并发获取连接。<br/>
     * 特别地，由于最小空闲数的存在，基本上都不会令阻塞队列在获取资源时发生阻塞。<br/>
     * 之所以要用一个线程来监视获取资源，是因为在本次操作中，实际上可以分为两层：
     * 拿不到第一把锁的所有线程将进入阻塞，假如已获得第一把锁的线程在想得到第二把锁的时候被阻塞，那么就会有死锁的可能性。
     * 故此时通过提供一个外部线程，无论如何都会在一定时间后使其释放锁，即通知他们超时。<br/>
     *
     * @return {@link Connection}
     */
    public Connection getConnection() {
        // 获取当前线程
        Thread currentThread = Thread.currentThread();
        // lambda表达式中捕获的值不可变，故用原子类来包装
        AtomicBoolean isTimeOut = new AtomicBoolean(false);
        // 获取当前线程的绑定资源
        Connection connection = threadLocal.get();
        // 若当前线程已绑定连接资源，则直接返回
        if (connection != null) {
            return connection;
        }

        Thread timer = new Thread(() -> {
            long start = System.currentTimeMillis();// 开始记录时间
            while (!Thread.currentThread().isInterrupted()) {
                if ((System.currentTimeMillis() - start) >= MAX_WAIT) {
                    // 若线程处于活动中，该方法不会产生任何影响。若线程在获取资源时被阻塞，则会中断阻塞。
                    currentThread.interrupt();
                    isTimeOut.set(true);// 当线程未被阻塞时，该值才是用于判断超时的
                }
            }
        });
        timer.setDaemon(true);// 设置为守护线程
        timer.start();

        while (!currentThread.isInterrupted()) {
            // 该处上锁是为了保证接下来的一系列事务的原子性，而不是为了保证数据修改的安全性
            synchronized (idlePool) {
                try {
                    // 判断超时，超时则直接抛出异常
                    if (isTimeOut.get()) throw new TimedOutException("连接超时");
                    isOverMaxActive();// 判断是否超过最大活跃数
                    isMinIdled();// 是否低于最小空闲数

                    connection = idlePool.take();// 获取资源
                    threadLocal.set(connection);// 线程绑定资源

                    presentMaxActive.increment();// 活跃数量累加器自增
                    activePool.put(connection, new ConnectionStatus());// 将该连接放入活跃连接池

                    return connection;// 最后返回资源

                } catch (InterruptedException e) {// 一般来说，只要最小连接数量不为负数，就不会出现阻塞的情况
                    logger.info("获取资源被阻塞，导致超时");
                    throw new TimedOutException("获取资源被阻塞，导致超时");
                }
                // 以下代码可以继续向上抛出，直到抛给controller层来调度view层展示信息
                catch (RuntimeException e) {
                    logger.info("连接失败:" + e.getMessage());// 记录日志
                    throw e;
                }
            }
        }
        return null;
    }

    @Override
    public Connection getConnection(String username, String password) {
        return null;
    }


    /**
     * 用于获取连接的代理类。<br/>
     * <p/>
     * 代理方法中主要增加了：在调用代理对象的{@code close()}时，
     * 调用的是本类{@code DataBasePool}的方法——{@code reclaim()}，特别地，需要将连接的事务重置，
     * 即掉调用{@link Connection#setAutoCommit(boolean)}并设置为true。<br/>
     * 在调用{@link Connection#isClosed()}方法时，将调用本类的{@code isReleased()}方法。<br/>
     * @return {@link Connection} 实际上返回的是一个连接的代理类
     */
    private Connection getConnectionProxy() {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class},
                new InvocationHandler() {
                    /**
                     * connection为代理类中的被代理对象。<br/>
                     * 用final修饰保证该对象不会再指向其他连接资源，
                     * 提高了池中的连接资源被反复获取与回收时的安全性。<br/>
                     * 不使用static的原因是，每个代理对象中，应该持有独有的一份连接资源，而不应该是共享的。<br/>
                     */
                    private final Connection connection;
                    private static final String CLOSE = "close";
                    private static final String IS_CLOSED = "isClosed";

                    {
                        try {
                            connection = DriverManager.getConnection(URL, USER, PASSWORD);// 用于获取资源
                        } catch (SQLException e) {
                            throw new RuntimeException("连接资源入池失败");
                        }
                    }

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 如果调用的是close方法，则调用自定义数据库连接池的release方法
                        if (CLOSE.equals(method.getName()) ) {
                            // 注意需要将连接的事务进行重置
                            ((Connection) proxy).setAutoCommit(true);
                            return release((Connection) proxy);
                        }
                        // 如果调用的是isClosed方法，则调用自定义数据库连接池的isReleased方法
                        else if (IS_CLOSED.equals(method.getName())) {
                            return isReleased((Connection) proxy);
                        }
                        // 如果是其他方法则无所谓了
                        return method.invoke(connection, args);
                    }
                });

    }

    /**
     * 用于回收资源到空闲池<br/>
     *
     * @param connection 需要回收的连接资源
     */
    private Void release(Connection connection) {
        threadLocal.remove();// 将当前线程解除资源绑定
        activePool.remove(connection);// 将连接资源移出活跃连接池
        try {
            idlePool.put(connection);// 将连接资源放回池中
        } catch (InterruptedException e) {
            // 一般情况下不会出现该异常。因为一定会放回无界队列中
            throw new RuntimeException("中断阻塞队列的阻塞作用");
        }
        presentMaxActive.decrement();// 活跃数量累加器自减
        return null;
    }

    /**
     * 用于判断一个连接资源是否从活跃连接池回到空闲连接池。<br/>
     *
     * @param connection 需要进行判断的连接资源
     * @return Boolean true已释放，false为未释放
     */
    private Boolean isReleased(Connection connection) {
        // 如果活跃连接池不存在该连接，则已释放，否则未释放
        return !activePool.containsKey(connection);
    }

    /**
     * 用于判断空闲连接数量是否低于{@code minIdle}。<br/>
     * <p/>
     * 在空闲连接数量小于等于{@code minIdle}时添加个数。个数cnt为 minIdle / 2 + 1。<br/>
     *
     * @throws MinIdledException    当空闲连接数量小于等于{@code minIdle}时，
     *                              抛出“已达最小空闲连接数量”异常。<br/>
     *                              但是异常不继续向上抛出，而是直接该方法内捕获。<br/>
     *                              故该方法不影响线程获取连接。<br/>
     * @throws InterruptedException 当阻塞队列到达边界线程阻塞，阻塞被打断时抛出异常。
     *                              但对于本方法来说不会出现此异常
     */
    private void isMinIdled() throws InterruptedException {
        if (idlePool.size() <= MIN_IDLE) {// 当达到最小空闲数量时，补充资源
            int cnt = MIN_IDLE / 2 + 1;// 设置补充资源的数量
            for (int i = 0; i < cnt; i++) {
                idlePool.put(getConnectionProxy());// 向池中添加资源
            }
            try {
                throw new MinIdledException("已到达最小空闲连接数");
            } catch (MinIdledException e) {
                logger.info(e.getMessage());
            }
        }
    }

    /**
     * 用于判断活跃连接数量是否大于{@code maxActive}<br/>
     * <p/>
     *
     * @throws OverMaxActiveException 当活跃连接数量大于{@code maxActive}时，
     *                                抛出“连接数量已达阈值”异常
     */
    private void isOverMaxActive() {
        if (presentMaxActive.sum() >= MAX_ACTIVE) {// 当达到最大活跃数量时抛出异常
            throw new OverMaxActiveException("连接数量已达阈值");
        }
    }


    @Override
    public <T> T unwrap(Class<T> interfaceClass) {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> interfaceClass) {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {

    }

    @Override
    public void setLoginTimeout(int seconds) {

    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() {
        return null;
    }

    /**
     * 用于囊括 将配置信息作为静态常量的静态类
     */
    static class ConfigConstants {
        private static final String DRIVER = "driver";
        private static final String URL = "url";
        private static final String USER = "username";
        private static final String PASSWORD = "password";
        private static final String INITIAL_SIZE = "initialSize";
        private static final String MIN_IDLE = "minIdle";
        private static final String MAX_ACTIVE = "maxActive";
        private static final String MAX_WAIT = "maxWait";
    }

    /**
     * 保存活跃连接资源的状态，如已存活时间，即被调用者持有的时间。
     */
    class ConnectionStatus {
        private long surviveTime;
        /*public ConnectionStatus openActiveConnection() {
        }*/

    }
}
