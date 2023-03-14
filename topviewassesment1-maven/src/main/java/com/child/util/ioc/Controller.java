/**
 * @创建时间 2021/7/10
 */
package com.child.util.ioc;

import com.child.util.ioc.annotation.JsAutowired;

public class Controller {

    @JsAutowired
    private Service service;

    public void work() {
        service.writeArticle("小奕Java");
    }

    public Service getService() {
        return service;
    }
}