package com.excelman.rpc.transport;

/**
 * @author Excelman
 * @date 2021/9/17 下午7:36
 * @description RpcServer的抽象接口定义
 */
public interface RpcServer {

    /**
     * 启动服务端
     */
    void start();

    /**
     * 向nacos发布服务
     * @param service
     * @param serviceClass
     * @param <T>
     */
    <T> void publishService(Object service, Class<T> serviceClass);
}
