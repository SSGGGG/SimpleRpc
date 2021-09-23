package com.excelman.rpc.provider;

/**
 * @author Excelman
 * @date 2021/9/14 上午11:13
 * @description 职责：保存“接口名——具体实现服务类”关联关系的容器
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
