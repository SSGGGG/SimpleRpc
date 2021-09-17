package com.excelman.rpc;

/**
 * @author Excelman
 * @date 2021/9/13 下午7:12
 * @description API接口定义，由server端实现，client端请求调用
 */
public interface HelloService{
    String hello(HelloObject object);
}
