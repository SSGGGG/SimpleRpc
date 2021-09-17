package com.excelman.rpc;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * @author Excelman
 * @date 2021/9/13 下午8:24
 * @description RPC服务端，负责接口rpc请求，并调用方法返回rpc结果
 * （这里采用线程池创建每一个线程处理）
 */
public class RpcServer {

    private final ExecutorService threadPool;

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    public RpcServer() {
        int corePoolSize = 5;
        int maximumPoolSize = 50;
        long keepAliveTime = 60;
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, queue, threadFactory);
    }

    /**
     * 注册Service接口，并创建Socket监听指定端口，当socket连接的时候，创建工作线程执行
     */
    public void register(Object service, int port){
        // Socket编程
        try(ServerSocket serverSocket = new ServerSocket(port)){
            logger.info("服务正在启动");
            Socket socket;
            while((socket = serverSocket.accept()) != null){
                logger.info("客户端连接，IP为：" + socket.getInetAddress());
                threadPool.execute(new WorkThread(socket, service));
            }
        } catch (IOException e) {
            logger.info("调用register方法出错 " + e);
        }
    }

}

/**
 * 工作线程：在run()方法中负责从socket读取RpcRequest解析出调用的method，执行method.invoke得到结果，封装为RpcResponse返回到socket中
 */
class WorkThread implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(WorkThread.class);

    private Socket socket;

    /**
     * API接口
     */
    private Object service;

    public WorkThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    @Override
    public void run() {
        try(ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())
        ){
            RpcRequest rpcRequest = (RpcRequest) inputStream.readObject();
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            Object returnObject = method.invoke(service, rpcRequest.getParameters());

            outputStream.writeObject(RpcResponse.success(returnObject));
            outputStream.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            logger.info("RpcServer调用run()方法出错 " + e);
        }
    }
}
