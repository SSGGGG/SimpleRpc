package com.excelman.rpc.transport.netty.server;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.enumeration.ResponseCode;
import com.excelman.rpc.provider.DefaultServiceProvider;
import com.excelman.rpc.provider.ServiceProvider;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Excelman
 * @date 2021/9/22 上午11:24
 * @description 该处理器负责接收RpcRequest，执行远程调用的方法，并返回结果
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RequestHandler requestHandler;
    private static ServiceProvider serviceProvider;

    static{
        requestHandler = new RequestHandler();
        serviceProvider = new DefaultServiceProvider();
    }

    /**
     * 通道读取
     * 当Netty通道读取到数据的时候（即服务端读取到RpcRequest），调用该方法
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        try{
            /* 检查是否是心跳包 */
            if(rpcRequest.getIsHeartBeat()){
                logger.info("服务端接收到心跳包...");
                return;
            }
            logger.info("服务端接收到消息:{}",rpcRequest);
            Object service = serviceProvider.getServiceProvider(rpcRequest.getInterfaceName());
            Object result = requestHandler.handle(rpcRequest, service);

            ChannelFuture future = ctx.writeAndFlush(RpcResponse.success(result));
            future.addListener(ChannelFutureListener.CLOSE);
        } finally {
            // 减小引用数
            ReferenceCountUtil.release(rpcRequest);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("NettyServer在handler处理过程中发生异常：{}", cause);
        ctx.close();
    }

    /**
     * 搭配心跳检测使用，当IdleStateHandler不满足条件的时候，会调用该方法
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if(state == IdleState.READER_IDLE){
                logger.error("长时间没有收到心跳包，自动断开连接...");
                ctx.close();
            }
        }else {
            super.userEventTriggered(ctx, evt);
        }
    }
}

/**
 * @author Excelman
 * @date 2021/9/22 下午3:55
 * @description 处理请求的具体实现类，职责：反射调用指定的类.方法()，并返回结果
 */
class RequestHandler {

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