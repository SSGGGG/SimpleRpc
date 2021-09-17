package com.excelman.serverTest;

import com.excelman.rpc.HelloService;
import com.excelman.rpc.RpcServer;
import com.excelman.rpc.server.HelloServiceImpl;

/**
 * @author Excelman
 * @date 2021/9/13 下午8:51
 * @description 服务测试端
 */
public class ServerTest {
    public static void main(String[] args) {
        // 1. 创建服务service
        HelloService service = new HelloServiceImpl();
        // 2. 注册到rpc-server中,指定端口号,实现暴露接口
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(service, 9050);
    }
}
