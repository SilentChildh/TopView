/**
 * @创建时间 2021/7/10
 */
package com.child.util.ioc;

public class IocTest {

    /**
     * 容器作为应用域对象
     */
    private static JsSampleContainer jsContainer = new JsSampleContainer();


    public static void main(String[] args) {
        /*逻辑操作*/
        // 将类注入容器
        jsContainer.registerBean(Controller.class);
        // 初始化注入-扫描引用
        jsContainer.initAutoWired();

        /*业务操作*/
        // 容器获取bean
        Controller client = jsContainer.getBean(Controller.class);
        // 执行
        client.work();
    }
}
