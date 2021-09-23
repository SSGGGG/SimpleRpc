package com.excelman.serverTest;

import com.excelman.rpc.RpcServer;
import com.excelman.rpc.provider.DefaultServiceRegistry;
import com.excelman.rpc.provider.ServiceRegistry;
import com.excelman.rpc.server.HelloServiceImpl;

/**
 * @author Excelman
 * @date 2021/9/13 下午8:51
 * @description 服务测试端
 */
public class ServerTest {
    public static void main(String[] args) {
        // 1. 创建服务注册器，并注册具体的服务实现类
        ServiceRegistry registry = new DefaultServiceRegistry();
        registry.register(new HelloServiceImpl());

        // 2. 创建Rpc服务端
        RpcServer server = new RpcServer(registry);
        server.start(9050);
    }
}
