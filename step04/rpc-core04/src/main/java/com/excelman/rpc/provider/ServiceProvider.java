package com.excelman.rpc.provider;

/**
 * @author Excelman
 * @date 2021/9/14 上午11:13
 * @description 职责：作为服务注册容器，保存和提供服务对象。将原先RpcServer调用register()方法，注册服务的工作转接到这里
 */
public interface ServiceProvider {

    /**
     * 注册服务
     */
    <T> void registerProvider(T service);

    /**
     * 根据接口服务名获取服务
     */
    Object getServiceProvider(String serviceName);
}
