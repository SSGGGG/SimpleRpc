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
     * scan service and register
     */
    void scanService();
}
