package com.excelman.clientTest;

import com.excelman.rpc.HelloObject;
import com.excelman.rpc.HelloService;
import com.excelman.rpc.transport.RpcClient;
import com.excelman.rpc.transport.RpcClientProxy;
import com.excelman.rpc.transport.netty.client.NettyClient;

/**
 * @author Excelman
 * @date 2021/9/22 下午4:32
 * @description
 */
public class ClientTest {

    public static void main(String[] args) {
        // 1. 创建客户端，以及通过代理获取HelloService
        RpcClient client = new NettyClient("127.0.0.1", 9500);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService proxy = rpcClientProxy.getProxy(HelloService.class);

        // 2. 调用代理对象的hello方法（即通过代理远程调用服务端实现的方法）
        HelloObject object = new HelloObject(1, "测试netty");
        String result = proxy.hello(object);
        System.out.println(result);
    }
}
