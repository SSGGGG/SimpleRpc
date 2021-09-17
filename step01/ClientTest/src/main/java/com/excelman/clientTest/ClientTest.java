package com.excelman.clientTest;

import com.excelman.rpc.HelloObject;
import com.excelman.rpc.HelloService;
import com.excelman.rpc.RpcClientProxy;

/**
 * @author Excelman
 * @date 2021/9/13 下午8:51
 * @description 客户测试端
 */
public class ClientTest {
    public static void main(String[] args) {
        // 1. 利用RpcClient动态代理创建代理对象
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9050);
        HelloService helloService = proxy.getProxy(HelloService.class);

        // 2. 传输参数，远程调用方法
        HelloObject helloObject = new HelloObject(1, "测试远程调用");
        String returnResult = helloService.hello(helloObject);
        System.out.println(returnResult);
    }
}
