## DataBasePool

### 静态代码块

~~~java
static {
    try {
        // 注册数据库驱动
        Class.forName(driver);
    } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
    }

    // 获取初始化资源
    for (int i = 0; i < initialSize; i++) {
        queue.offer(getConnectionProxy());
    }
    logger.info("数据库连接池初始化完毕");
}
~~~

### getConnection()

~~~java
    /**
     * 用于从空闲池中获取连接资源。<br/>
     * <p/>
     * 内置局部监听线程listener，用于监听每个线程获取连接的时间。<br/>
     * 以{@code queue}为锁，防止多线程并发获取连接。<br/>
     *
     * @return {@link Connection}
     */
    public static Connection getConnection() {
        Connection connection = threadLocal.get();// 获取线程绑定资源

        // 若当前线程已绑定连接资源，则直接返回
        if (connection != null) {
            return connection;
        }

        // 监听线程，用于监听是否连接超时
        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();// 开始记录时间
                // 不会出现阻塞情况，故不需要捕获InterruptedException
                while (!Thread.interrupted()) {
                    // 当前时间与start变量的相差maxWait时标记isTimedOut为真，并直接销毁线程
                    if ((System.currentTimeMillis() - start) > maxWait) {
                        isTimedOut = true;
                        return;
                    }
                }
            }
        }, "监听获取数据库连接时长");
        // 开启计时器
        listener.start();
        while (true) {
            synchronized (queue) {
                /*模拟超时，但是这并未能真实模拟多线程争夺资源的情境
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }*/

                try {
                    // 判断是否超过最大活跃数、低于最小空闲数、获取超时
                    isOverMaxActive();
                    isMinIdled();
                    isTimedOut();

                    // 通过检验，先进行线程资源绑定。再返回一个连接资源，并令活跃连接数量自增
                    connection = queue.poll();
                    threadLocal.set(connection);
                    presentMaxActive++;
                    return connection;

                }
                // 以下代码可以继续向上抛出，直到抛给controller层来调度view层展示信息
                catch (RuntimeException e) {
                    logger.info("连接失败:" + e.getMessage());// 记录日志
                    throw e;
                } finally {
                    listener.interrupt();// 该方法返回时中断线程

                    isTimedOut = false;// 重置超时标记
                }
            }
        }
    }
~~~

### getConnectionProxy()

~~~java
    /**
     * 用于获取连接的代理类。<br/>
     * <p/>
     * 代理方法中主要增加了：在调用代理对象的{@code close()}时，
     * 调用的是本类{@code DataBasePool}的方法——{@code reclaim()}。<br/>
     *
     * @return {@link Connection} 实际上返回的是一个连接的代理类
     */
    private static Connection getConnectionProxy() {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class},
                new InvocationHandler() {
                    /**
                     * connection为代理类中的被代理对象。<br/>
                     * 用final修饰保证该对象不会再指向其他连接资源，
                     * 提高了池中的连接资源被反复获取与回收时的安全性。<br/>
                     * 不使用static的原因是，每个代理对象中，应该持有独有的一份连接资源，而不应该是共享的。<br/>
                     */
                    private final Connection connection;

                    {
                        try {
                            connection = DriverManager.getConnection(url, user, pwd);// 用于获取资源
                        } catch (SQLException e) {
                            throw new RuntimeException("连接资源入池失败");
                        }
                    }

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 如果调用的是close方法，则调用自定义数据库连接池的reclaim方法
                        if ("close".equals(method.getName())) {
                            return reclaim(connection);
                        }
                        // 如果是其他方法则无所谓了
                        return method.invoke(connection, args);
                    }
                });

    }
~~~

### reclaim(Connection connection)

~~~java
	/**
     * 用于回收资源到空闲池<br/>
     * <p/>
     * 上锁的主要目的是同时保证可见性与原子性。<br/>
     * @param connection 需要回收的连接资源
     */
    public static Void reclaim(Connection connection) {
        threadLocal.remove();// 将当前线程解除资源绑定
        queue.offer(connection);// 将连接资源放回池中
        synchronized (presentMaxActive) {// 写锁，保证操作活跃数量的原子性
            presentMaxActive--;// 当前活跃数量自减
        }
        return null;
    }
~~~

### isMinIdled()

~~~java
/**
     * 用于判断空闲连接数量是否低于{@code minIdle}。<br/>
     * <p/>
     * 在空闲连接数量小于等于{@code minIdle}时添加个数。个数cnt为 minIdle / 2 + 1。<br/>
     *
     * @throws MinIdledException 当空闲连接数量小于等于{@code minIdle}时，
     *                           抛出“已达最小空闲连接数量”异常。<br/>
     *                           但是异常不继续向上抛出，而是直接该方法内捕获。<br/>
     *                           故该方法不影响线程获取连接。<br/>
     */
    private static void isMinIdled() {// 当达到最小空闲数量时，补充资源
        if (queue.size() <= minIdle) {
            int cnt = minIdle / 2 + 1;// 设置补充资源的数量
            for (int i = 0; i < cnt; i++) {
                queue.offer(getConnectionProxy());// 向池中添加资源
            }
            try {
                throw new MinIdledException("已到达最小空闲连接数");
            } catch (MinIdledException e) {
                logger.info(e.getMessage());
            }
        }
    }
~~~

### isOverMaxActive()

~~~~java
/**
     * 用于判断活跃连接数量是否大于{@code maxActive}<br/>
     * <p/>
     *
     * @throws OverMaxActiveException 当活跃连接数量大于{@code maxActive}时，
     *                                抛出“连接数量已达阈值”异常
     */
    private static void isOverMaxActive() {
        if (presentMaxActive >= maxActive) {// 当达到最大活跃数量时抛出异常
            throw new OverMaxActiveException("连接数量已达阈值");
        }
    }
~~~~

### isTimedOut()

~~~java
/**
     * 用于判断获取连接是否超时{@code isTimedOut}<br/>
     * <p/>
     *
     * @throws TimedOutException 当获取连接超时{@code isTimedOut}，
     *                           则抛出“连接超时”异常
     */
    private static void isTimedOut() {
        if (isTimedOut) {// 当监听线程将isTimedOut设置为true时抛出异常
            throw new TimedOutException("连接超时");
        }
    }
~~~

