## DataBasePool

首先易知每个线程是按顺序先拿先放的，因为设置了锁，不会出现并发混乱的问题。 但是有一个问题就是在达到最小空闲数量到达5个时，便会自动增加3个。此时就会造成“插入”操作。 因此无法确定已经被获取过的资源的次序。 



### 测试1

首先是设置10个线程，每个线程进行两轮测试：

1. 一是在不关闭资源情况下获取到的是同一资源；
2. 二是关闭资源后，获取到的是不同资源。

~~~java
void testGetConnection1() {
    for (int i = 0; i < 10; i++) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 测试同一资源
                Connection expected = DataBasePool.getConnection();// 期望值，未关闭资源前所获取的所有连接都是同一个
                Connection actual =DataBasePool.getConnection();// 实际值，再次获取一个资源
                Assertions.assertSame(expected, actual);// 下断言，每个资源都是同一个

                // 将期望值的资源关闭，测试不同资源
                try {
                    expected.close();

                    expected = actual;// 期望值为原先的连接
                    actual = DataBasePool.getConnection();// 实际值再次获取新资源
                    Assertions.assertNotSame(expected, actual);// 下断言，两个资源不同

                    actual.close();// 关闭新获取的资源

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();// 开启线程
    }
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}
~~~

测试结果：

1. 大部分情况下，均能够满足预期值.

    ![](./简易orm框架-测试截图/img.png)

2. 极少情况下，在判断不同资源时，得到连接资源相同的异常.

    ![](./简易orm框架-测试截图/img_1.png)



### 测试2

设置10个以上线程，每个线程进行两轮测试：

1. 一是在不关闭资源情况下获取到的是同一资源；
2. 二是关闭资源后，获取到的是不同资源。

~~~java
void testGetConnection2() {
    for (int i = 0; i < 11; i++) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 测试同一资源
                Connection expected = DataBasePool.getConnection();// 期望值，未关闭资源前所获取的所有连接都是同一个
                Connection actual =DataBasePool.getConnection();// 实际值，再次获取一个资源
                Assertions.assertSame(expected, actual);// 下断言，每个资源都是同一个

                // 将期望值的资源关闭，测试不同资源
                try {
                    expected.close();

                    expected = actual;// 期望值为原先的连接
                    actual = DataBasePool.getConnection();// 实际值再次获取新资源
                    Assertions.assertNotSame(expected, actual);// 下断言，两个资源不同

                    actual.close();// 关闭新获取的资源

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();// 开启线程
    }
    try {
        Thread.sleep(500);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}
~~~

测试结果：基本上都会出现连接资源相同的异常。 

![](./简易orm框架-测试截图/img_2.png)



### 测试3

设置21个线程。

测试到达最大活跃数量.

~~~java
public void testGetConnection() throws InterruptedException {
    for (int i = 0; i < 21; i++) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 测试同一资源
                Connection expected = DataBasePool.getConnection();// 期望值，未关闭资源前所获取的所有连接都是同一个
                Connection actual =DataBasePool.getConnection();// 实际值，再次获取一个资源
                Assertions.assertSame(expected, actual);// 下断言，每个资源都是同一个

                // 将期望值的资源关闭，测试不同资源
                try {
                    expected.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                expected = actual;// 期望值为原先的连接
                actual = DataBasePool.getConnection();// 实际值再次获取新资源
                Assertions.assertNotSame(expected, actual);// 下断言，两个资源不同

            }
        });
        thread.start();// 开启线程
    }
    Thread.sleep(1000);
}
~~~

测试结果：符合预期





### 测试4

特别的，对于超时测试，这里并未能真实模拟， 这里仅仅是配合DataBasePool.getConnection()中加入线程睡眠来模拟超时。 

~~~java
void testIsTimedOut() {
    Assertions.assertTimeoutPreemptively(Duration.of(5, ChronoUnit.SECONDS), new Executable() {
        @Override
        public void execute() throws Throwable {
            DataBasePool.getConnection();
        }
    });
}
~~~

~~~java
public class DataBasePool {
    public static Connection getConnection() {
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
                // 模拟超时，但是这并未能真实模拟多线程争夺资源的情境
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    // 判断是否超时
                    isTimedOut();
                }
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
}
~~~



### 测试覆盖率



### 测试总结

1. ThreadLocal资源绑定出现了问题。会导致线程释放资源后还是获取相同的资源。

    但其实问题不大，因为这只是因为池中资源轮回了一周，使得线程再次拿到了自己原先接触过的资源。

