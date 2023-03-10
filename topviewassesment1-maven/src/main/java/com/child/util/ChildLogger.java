package com.child.util;

import java.util.logging.Logger;

/**
 * 用于获取全局日志对象{@link Logger#global}<br/>
 * <p/>
 * 定义了一个jdk内置的全局的日志，用于打印整个系统中的信息<br/>
 * @author silent_child
 * @version 1.0
 **/

public class ChildLogger {
    private static final Logger logger = Logger.getGlobal();
    public static Logger getLogger() {
        return logger;
    }
}
