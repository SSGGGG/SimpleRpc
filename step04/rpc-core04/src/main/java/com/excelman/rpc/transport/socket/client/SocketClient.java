package com.excelman.rpc.transport.socket.client;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.registry.NacosServiceRegistry;
import com.excelman.rpc.registry.ServiceRegistry;
import com.excelman.rpc.transport.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Excelman
 * @date 2021/9/13 下午8:05
 * @description RPC客户端，负责发送请求，返回响应结果
 */
public class SocketClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private ServiceRegistry serviceRegistry;

    public SocketClient(){
        serviceRegistry = new NacosServiceRegistry();
    }

    /**
     * 通过Socket编程，向指定host + port的服务端发送rpcRequest请求，并得到返回结果
     * @param rpcRequest
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest){
        /* 通过RpcRequest中的调用接口名，在nacos服务发现中找到host和port */
        String interfaceName = rpcRequest.getInterfaceName();
        InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(interfaceName);
        String host = inetSocketAddress.getHostName();
        int port = inetSocketAddress.getPort();

        try(Socket socket = new Socket(host, port)){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();

            logger.info("客户端发送消息:{}",rpcRequest.toString());
            // 服务端返回一个RpcResponse对象
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.info("调用sendRequest方法出错：" + e);
            return null;
        }
    }
}
