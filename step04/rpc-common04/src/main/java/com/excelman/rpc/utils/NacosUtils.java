package com.excelman.rpc.utils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Excelman
 * @date 2021/9/23 下午2:16
 * @description 管理Nacos连接等的工具类
 */
public class NacosUtils {

    private static final Logger logger = LoggerFactory.getLogger(NacosUtils.class);

    private static final String NACOS_ADDRESS = "127.0.0.1:8848";

    private static final NamingService namingService;

    /* 注册到Nacos的服务及其地址 */
    private static final Map<String, InetSocketAddress> service = new HashMap<>();

    static{
        namingService = getNamingService();
    }

    private static NamingService getNamingService(){
        try {
            return NamingFactory.createNamingService(NACOS_ADDRESS);
        } catch (NacosException e) {
            logger.error("Nacos创建NamingService出现异常：{}",e);
        }
        return null;
    }

    /**
     * 注册接口服务到Nacos中，并添加到本地map中
     */
    public static void register(String serviceName, String host, int port){
        try {
            namingService.registerInstance(serviceName, host, port);
            service.put(serviceName, new InetSocketAddress(host, port));
        } catch (NacosException e) {
            logger.error("Nacos注册服务出现异常:{}",e);
        }
    }

    /**
     * 服务器线程关闭，注销nacos的服务连接
     */
    public static void deRegister(){
        for(Map.Entry<String, InetSocketAddress> entry : service.entrySet()){
            try{
                InetSocketAddress value = entry.getValue();
                namingService.deregisterInstance(entry.getKey(), value.getHostName(), value.getPort());
            } catch (NacosException e) {
                logger.error("销毁服务 {} 的时候出现了异常:{}",entry.getKey(), e);
            }
        }
    }
}
