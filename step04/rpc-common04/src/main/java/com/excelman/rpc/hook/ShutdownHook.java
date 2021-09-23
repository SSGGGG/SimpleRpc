package com.excelman.rpc.hook;

import com.excelman.rpc.utils.NacosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Excelman
 * @date 2021/9/23 下午2:49
 * @description 单例方法获取钩子，通过钩子注销nacos所有服务
 */
public class ShutdownHook {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHook.class);

    private static volatile ShutdownHook shutdownHook;

    public static ShutdownHook getShutdownHook(){
        if(shutdownHook == null){
            synchronized (ShutdownHook.class){
                if(shutdownHook == null){
                    shutdownHook = new ShutdownHook();
                }
            }
        }
        return shutdownHook;
    }

    /**
     * 向JVM虚拟机注入钩子，当系统shutdown的时候，将注销所有Nacos服务
     */
    public void clearAllNacosService(){
        logger.info("调用之后，将注销Nacos全部服务");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            NacosUtils.deRegister();
        }));
    }

}
