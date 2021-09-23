package com.excelman.rpc.transport.socket.server;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.enumeration.ResponseCode;
import com.excelman.rpc.provider.DefaultServiceProvider;
import com.excelman.rpc.provider.ServiceProvider;
import com.excelman.rpc.transport.RpcServer;
import com.excelman.rpc.transport.socket.AbstractRpcServer;
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
 * @description RPC服务端
 */
public class SocketServer extends AbstractRpcServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUN_POOL_SIZE = 50;
    private static final long KEEP_ALIVE_TIME = 60;
    private final ExecutorService threadPool;
    private DefaultServiceProvider registry = new DefaultServiceProvider();

    public SocketServer(String host, int port) {
        this.host = host;
        this.port = port;

        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUN_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, queue, threadFactory);
    }

    @Override
    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(this.port)){
            logger.info("服务端开启");
            Socket socket;
            while((socket = serverSocket.accept()) != null){
                logger.info("客户端连接：{},{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket, registry));
            }
        } catch (IOException e) {
            logger.error("服务端socket连接发生异常：{}",e);
        } finally {
            threadPool.shutdown();
        }
    }
}

/**
 * 请求处理线程：
 *  从socket中获取输入流的RpcRequest，解析得到调用的接口服务，通过服务注册器获取接口服务对应的具体服务类
 *  创建具体的RequestHandler，执行反射方法，返回结果
 */
class RequestHandlerThread implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

    private Socket socket;
    private ServiceProvider serviceProvider;
    private RequestHandler requestHandler;

    public RequestHandlerThread(Socket socket, ServiceProvider serviceProvider) {
        this.socket = socket;
        this.serviceProvider = serviceProvider;
        this.requestHandler = new RequestHandler();
    }

    @Override
    public void run() {
        try(ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())){
            // 根据客户端调用的接口名，从服务注册类中获取具体的实现类
            RpcRequest rpcRequest = (RpcRequest) inputStream.readObject();
            String interfaceName = rpcRequest.getInterfaceName();
            Object service = serviceProvider.getServiceProvider(interfaceName);
            // 通过RequestHandler，执行具体的反射方法
            Object result = requestHandler.handle(rpcRequest, service);
            // 将结果返回给客户端
            outputStream.writeObject(RpcResponse.success(result));
            outputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 真正处理请求的处理器：处理请求，执行反射方法并返回结果
 */
class RequestHandler{

    /**
     * 执行反射方法，返回结果
     * @param service 具体服务类
     * @return
     */
    public Object handle(RpcRequest rpcRequest, Object service){
        Object result = null;
        try{
            result = handleInvoke(rpcRequest, service);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Object handleInvoke(RpcRequest rpcRequest, Object service) throws InvocationTargetException, IllegalAccessException {
        Method method = null;
        try{
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NO_FOUND);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }

}