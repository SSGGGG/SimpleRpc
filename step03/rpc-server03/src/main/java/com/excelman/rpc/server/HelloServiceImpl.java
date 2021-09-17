package com.excelman.rpc.server;

import com.excelman.rpc.HelloObject;
import com.excelman.rpc.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Excelman
 * @date 2021/9/13 下午7:15
 * @description 服务端：API接口的实现类
 */
public class HelloServiceImpl implements HelloService {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到消息：" + object.getMessage());
        return "调用的返回值,id = " + object.getId();
    }
}
