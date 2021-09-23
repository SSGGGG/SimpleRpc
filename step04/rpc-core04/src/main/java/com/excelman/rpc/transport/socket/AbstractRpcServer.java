package com.excelman.rpc.transport.socket;

import com.excelman.rpc.annotation.Service;
import com.excelman.rpc.annotation.ServiceScan;
import com.excelman.rpc.enumeration.RpcError;
import com.excelman.rpc.exception.RpcException;
import com.excelman.rpc.provider.ServiceProvider;
import com.excelman.rpc.registry.ServiceRegistry;
import com.excelman.rpc.transport.RpcServer;
import com.excelman.rpc.utils.NacosUtils;
import com.excelman.rpc.utils.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author Excelman
 * @date 2021/9/23 下午5:09
 * @description 为了抽取scanService()方法，因此抽象出该类
 */
public abstract class AbstractRpcServer implements RpcServer {

    /*
     * 采用protected，可以抽象出子类的公共属性
     */

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    protected String host;
    protected int port;

    protected ServiceRegistry serviceRegistry;
    protected ServiceProvider serviceProvider;

    /**
     * 1. 获取启动类名称，并通过反射获取该类，判断是否有ServiceScan注解
     * 2. 若有该注解，则获取注解的value值，即为扫描入口的包
     * 3. 通过调用ReflectUtils.getClasses(packageName)方法，获取该包及其子包中的所有class
     * 4. 遍历得到的class，判断当前class是否有Service注解，若有的话，则执行注册
     */
    @Override
    public void scanService() {
        String startClassName = ReflectUtils.getStartClassName();
        Class<?> startClass = null;
        try {
            startClass = Class.forName(startClassName);
        } catch (ClassNotFoundException e) {
            logger.error("扫描Service的时候发生异常：{}",e);
        }
        if(!startClass.isAnnotationPresent(ServiceScan.class)){
            throw new RpcException(RpcError.NO_ANNOTATION_SERVICE_SCAN);
        }
        String packageName = startClass.getAnnotation(ServiceScan.class).packageName();
        if("".equals(packageName)){
            packageName = startClassName.substring(0, startClassName.lastIndexOf('.'));
        }
        Set<Class<?>> allClasses = ReflectUtils.getClasses(packageName);
        for(Class<?> clazz : allClasses){
            if(clazz.isAnnotationPresent(Service.class)){
                String serviceName = clazz.getAnnotation(Service.class).name(); // 服务接口名
                Object obj = null;
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建对象 {} 实例的时候，发生异常：{}", clazz.getName(), e);
                }
                if("".equals(serviceName)){ // 此时将获取当前实现类的所有接口
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for(Class<?> inter : interfaces){
                        publishService(obj, inter.getCanonicalName());
                    }
                }else{
                    publishService(obj, serviceName);
                }
            }
        }
    }

    /**
     * 将service及其实现的接口注册到本地Provider的map中，再将具体实现类及其地址注册到NacosRegistry中
     *
     * @param service 具体服务实现类
     * @param serviceClass 服务的接口类型名
     * @param <T>
     */
    private <T> void publishService(Object service, String serviceClass) {
        serviceProvider.registerProvider(service);
        serviceRegistry.register(serviceClass, host, port);
    }
}
