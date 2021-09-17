package com.excelman.rpc;

import com.excelman.rpc.entity.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Excelman
 * @date 2021/9/13 下午8:05
 * @description RPC客户端，负责发送请求，返回响应结果
 */
public class RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    /**
     * 通过Socket编程，向指定host + port的服务端发送rpcRequest请求，并得到返回结果
     * @param rpcRequest
     * @param host
     * @param port
     * @return
     */
    public Object sendRequest(RpcRequest rpcRequest, String host, int port){
        try(Socket socket = new Socket(host, port)){
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();

            // 服务端返回一个RpcResponse对象
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.info("调用sendRequest方法出错：" + e);
            return null;
        }
    }
}
