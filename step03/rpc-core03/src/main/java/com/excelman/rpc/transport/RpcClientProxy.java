package com.excelman.rpc.transport;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.transport.netty.client.NettyClient;
import com.excelman.rpc.transport.socket.client.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Excelman
 * @date 2021/9/13 下午7:40
 * @description 客户端的实现——采用JDK动态代理生成实例对象，被代理对象调用方法时调用invoke方法
 */
public class RpcClientProxy implements InvocationHandler {

    private final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    private RpcClient rpcClient;

    public RpcClientProxy(RpcClient rpcClient){
        this.rpcClient = rpcClient;
    }

    /**
     * 获得代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 被代理对象调用方法时触发的invoke操作：
     *      在该方法中，需要生成RpcRequest传给对应的服务端，再创建RpcResponse接受服务端传输过来的结果
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.info("客户端远程调用方法:{}#{}", method.getDeclaringClass().getName(), method.getName());
        // 创建RpcRequest对象
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setInterfaceName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameters(args);
        rpcRequest.setParamTypes(method.getParameterTypes());
        // 发送rpcRequest，接收rpcResponse
        RpcResponse response = null;
        if(rpcClient instanceof SocketClient){
            // 传回的是一个RpcResponse
            response = (RpcResponse) rpcClient.sendRequest(rpcRequest);
        }else if(rpcClient instanceof NettyClient){
            // 传回的是RpcResponse.getData()
            return rpcClient.sendRequest(rpcRequest);
        }
        return response.getData();
    }
}
