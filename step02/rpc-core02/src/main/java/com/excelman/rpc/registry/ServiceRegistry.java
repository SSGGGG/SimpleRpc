package com.excelman.rpc.registry;

/**
 * @author Excelman
 * @date 2021/9/14 上午11:13
 * @description 服务注册容器，将原先RpcServer调用register()方法，注册服务的工作转接到这里
 */
public interface ServiceRegistry {

    /**
     * 注册
     */
    <T> void register(T service);

    /**
     * 根据服务名获取服务
     */
    Object getService(String serviceName);
}
