package com.excelman.servetTest;

import com.excelman.rpc.HelloService;
import com.excelman.rpc.registry.DefaultServiceRegistry;
import com.excelman.rpc.server.HelloServiceImpl;
import com.excelman.rpc.transport.netty.server.NettyServer;

/**
 * @author Excelman
 * @date 2021/9/22 下午4:32
 * @description
 */
public class ServerTest {

    public static void main(String[] args) {
        // 1. 注册服务
        HelloService service = new HelloServiceImpl();
        DefaultServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(service);

        // 2. 创建NettyServer，并启动
        new NettyServer().start(9500);
    }
}
