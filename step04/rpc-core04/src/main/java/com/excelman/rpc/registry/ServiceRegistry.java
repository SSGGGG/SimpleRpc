package com.excelman.rpc.registry;

import java.net.InetSocketAddress;

/**
 * @author Excelman
 * @date 2021/9/23 上午9:34
 * @description 服务注册接口定义
 */
public interface ServiceRegistry {

    /**
     * 注册服务及其地址到Nacos中
     * @param serviceName 接口服务名
     * @param socketAddress 地址
     */
    void register(String serviceName, InetSocketAddress socketAddress);

    /**
     * 从Nacos中根据服务名获取地址
     * @param serviceName 接口服务名
     * @return
     */
    InetSocketAddress lookupService(String serviceName);
}
