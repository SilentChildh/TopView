package com.child.util.orm;

import com.child.dao.UserDAO;
import com.child.util.ChildLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ParametersHandlerTest {
    private static final Logger logger = ChildLogger.getLogger();

    /**
     * 测试参数处理器。<br/>
     * <p/>
     * 以{@link com.child.dao.UserDAO#updateById(Long, String, String)}作为测试.<br/>
     */
    @Test
    void handle() {
        /*准备*/
        Class<UserDAO> userDAOClass = UserDAO.class;
        String methodName = "updateById";
        Object[] args = {225L, "李四", "特斯拉"};

        /*创建参数处理器， 并处理*/
        ParametersHandler parametersHandler = new ParametersHandler(methodName, userDAOClass, args);
        Object handle = parametersHandler.handle();
        Assertions.assertNotNull(handle);

        logger.info(handle.toString());
    }

}