package com.excelman.clientTest;

import com.excelman.rpc.HelloObject;
import com.excelman.rpc.HelloService;
import com.excelman.rpc.serializer.CommonSerializer;
import com.excelman.rpc.transport.RpcClient;
import com.excelman.rpc.transport.RpcClientProxy;
import com.excelman.rpc.transport.netty.client.NettyClient;

/**
 * @author Excelman
 * @date 2021/9/23 上午10:47
 * @description 客户端测试
 */
public class NettyClientTest {

    // NOW: client select kryo serializer

    public static void main(String[] args) throws InterruptedException {
        // 1. create RpcClient
        RpcClient rpcClient = new NettyClient(CommonSerializer.getByCode(CommonSerializer.KRYO_SERIALIZER));

        // 2. create ClientProxy, and then create Service(HelloService)
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        HelloService proxy = rpcClientProxy.getProxy(HelloService.class);

        // 3. use proxy service's method
        for(int i=0; i<2; i++){
            String result = proxy.hello(new HelloObject(1, "TestNacosRegistry"));
            System.out.println("返回结果：" + result);

            Thread.sleep(1000);

        }
    }
}
