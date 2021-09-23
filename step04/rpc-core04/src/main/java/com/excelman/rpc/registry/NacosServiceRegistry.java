package com.excelman.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.excelman.rpc.enumeration.RpcError;
import com.excelman.rpc.exception.RpcException;
import com.excelman.rpc.loadbalancer.LoadBalancer;
import com.excelman.rpc.loadbalancer.RandomLoadBalancer;
import com.excelman.rpc.utils.NacosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Excelman
 * @date 2021/9/23 上午9:38
 * @description Nacos服务注册与发现中心
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

    private LoadBalancer loadBalancer;

    public NacosServiceRegistry() {
        this(null);
    }

    public NacosServiceRegistry(LoadBalancer loadBalancer) {
        if(null == loadBalancer){
            this.loadBalancer = new RandomLoadBalancer();
        }else{
            this.loadBalancer = loadBalancer;
        }
    }

    @Override
    public void register(String serviceName, String host, int port) {
        NacosUtils.register(serviceName, host, port);
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            List<Instance> allInstances = namingService.getAllInstances(serviceName);
            /* 这里可以引入负载均衡策略获取实例对象 */
            Instance instance = loadBalancer.select(allInstances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            logger.error("发现服务{}过程中发生异常:{}",e);
            throw new RpcException(RpcError.DISVOCE_SERVICE_FAILED);
        }
    }
}
