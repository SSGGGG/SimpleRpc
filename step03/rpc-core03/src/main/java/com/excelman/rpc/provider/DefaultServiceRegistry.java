package com.excelman.rpc.provider;

import com.excelman.rpc.enumeration.RpcError;
import com.excelman.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Excelman
 * @date 2021/9/14 上午11:15
 * @description 默认的服务注册实现类
 */
public class DefaultServiceRegistry implements ServiceRegistry{

    private final Logger logger = LoggerFactory.getLogger(DefaultServiceRegistry.class);

    /**
     * 采用map存放 服务接口名——具体服务实现类
     */
    private final static Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    /**
     * 采用set存放 具体服务实现类名（用于判断是否注册）
     */
    private final static Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 注册服务
     * @param service 具体服务实现类
     */
    @Override
    public synchronized <T> void register(T service) {
        String serviceName = service.getClass().getCanonicalName();
        if(registeredService.contains(serviceName)) return;
        registeredService.add(serviceName);

        Class<?>[] interfaces = service.getClass().getInterfaces();
        if(interfaces.length == 0){
            throw new RpcException(RpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE);
        }
        for(Class<?> temp : interfaces){
            serviceMap.put(temp.getCanonicalName(), service);
        }
        logger.info("向接口：{}，注册服务{}", interfaces, serviceName);
    }

    /**
     * 根据服务接口名，获取实现的服务
     * @param serviceName 服务接口名
     * @return
     */
    @Override
    public synchronized Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if(null == service){
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service;
    }
}
