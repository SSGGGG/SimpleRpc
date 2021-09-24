package com.excelman.clientTest;

import com.excelman.rpc.HelloObject;
import com.excelman.rpc.HelloService;
import com.excelman.rpc.transport.RpcClient;
import com.excelman.rpc.transport.RpcClientProxy;
import com.excelman.rpc.transport.netty.client.NettyClient;

/**
 * @author Excelman
 * @date 2021/9/23 上午10:47
 * @description 客户端测试
 */
public class NettyClientTest {

    public static void main(String[] args) {
        // 1. create RpcClient
        RpcClient rpcClient = new NettyClient();

        // 2. create ClientProxy, and then create Service(HelloService)
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService proxy = rpcClientProxy.getProxy(HelloService.class);

        // 3. use proxy service's method
        for(int i=0; i<20; i++){
            String result = proxy.hello(new HelloObject(1, "TestNacosRegistry"));
            System.out.println("返回结果：" + result);
        }
    }
}
