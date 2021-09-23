package com.excelman.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.excelman.rpc.enumeration.RpcError;
import com.excelman.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Excelman
 * @date 2021/9/23 上午9:38
 * @description Nacos服务注册中心
 */
public class NacosServiceRegistry implements ServiceRegistry{

    private final static Logger logger = LoggerFactory.getLogger(NacosServiceRegistry.class);
    private final static String NACOS_ADDRESS = "127.0.0.1:8848";
    private final static NamingService namingService;

    static{
        try {
            namingService = NamingFactory.createNamingService(NACOS_ADDRESS);
        } catch (NacosException e) {
            logger.error("连接Nacos发生异常：{}",e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_SERVICE_REGISTRY);
        }
    }

    @Override
    public void register(String serviceName, InetSocketAddress socketAddress) {
        try {
            namingService.registerInstance(serviceName, socketAddress.getHostName(), socketAddress.getPort());
        } catch (NacosException e) {
            logger.error("注册nacos服务发生异常：{}",e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILED);
        }
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            /*这里可以引入负载均衡策略获取实例对象*/
            List<Instance> allInstances = namingService.getAllInstances(serviceName);
            Instance instance = allInstances.get(0);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            logger.error("发现服务{}过程中发生异常:{}",e);
            throw new RpcException(RpcError.DISVOCE_SERVICE_FAILED);
        }
    }
}
