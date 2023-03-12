package com.child.exception;

/**
 * 到达最小空闲数异常.<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/12
 */
public class MinIdledException extends RuntimeException{
    public MinIdledException(String message) {
        super(message);
    }

    public MinIdledException() {
    }
}
