package com.child.exception;

/**
 * 到达最大活跃数量异常。<br/>
 *
 * @author silent_child
 * @version 1.0.0
 * @date 2023/03/12
 */
public class OverMaxActiveException extends RuntimeException {
    public OverMaxActiveException(String message) {
        super(message);
    }

    public OverMaxActiveException() {
    }
}
