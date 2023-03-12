package com.child.exception;

/**
 * 超时异常.<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/12
 */
public class TimedOutException extends RuntimeException{
    public TimedOutException() {
    }

    public TimedOutException(String message) {
        super(message);
    }
}
