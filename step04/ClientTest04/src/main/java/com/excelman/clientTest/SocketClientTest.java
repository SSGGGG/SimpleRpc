package com.excelman.clientTest;

import com.excelman.rpc.HelloObject;
import com.excelman.rpc.HelloService;
import com.excelman.rpc.transport.RpcClient;
import com.excelman.rpc.transport.RpcClientProxy;
import com.excelman.rpc.transport.socket.client.SocketClient;

/**
 * @author Excelman
 * @date 2021/9/24 上午9:54
 * @description Socket的客户端测试
 */
public class SocketClientTest {

    public static void main(String[] args) {
        // 1. create SocketClient, and Proxy
        RpcClient client = new SocketClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService proxy = rpcClientProxy.getProxy(HelloService.class);

        // 2. use proxy's method and then get the response
        String result = proxy.hello(new HelloObject(1, "testSocketTransport"));
        System.out.println(result);
    }
}
