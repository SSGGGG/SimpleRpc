package com.excelman.rpc.transport.netty.server;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.handler.RequestHandler;
import com.excelman.rpc.registry.DefaultServiceRegistry;
import com.excelman.rpc.registry.ServiceRegistry;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Excelman
 * @date 2021/9/22 上午11:24
 * @description 该处理器负责接受RpcRequest，执行远程调用的方法，并返回结果
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);
    private static RequestHandler requestHandler;
    private static ServiceRegistry serviceRegistry;

    static{
        requestHandler = new RequestHandler();
        serviceRegistry = new DefaultServiceRegistry();
    }

    /**
     * 通道读取
     * 当Netty通道读取到数据的时候（即服务端读取到RpcRequest），调用该方法
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        try{
            logger.info("服务端接收到消息:{}",rpcRequest);
            Object service = serviceRegistry.getService(rpcRequest.getInterfaceName());
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
        logger.error("NettyServer在handler处理过程中发生异常：");
        cause.printStackTrace();
        ctx.close();
    }
}
