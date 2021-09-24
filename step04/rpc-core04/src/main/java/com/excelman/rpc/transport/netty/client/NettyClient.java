package com.excelman.rpc.transport.netty.client;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.enumeration.RpcError;
import com.excelman.rpc.exception.RpcException;
import com.excelman.rpc.loadbalancer.LoadBalancer;
import com.excelman.rpc.loadbalancer.RandomLoadBalancer;
import com.excelman.rpc.registry.NacosServiceRegistry;
import com.excelman.rpc.registry.ServiceRegistry;
import com.excelman.rpc.serializer.CommonSerializer;
import com.excelman.rpc.transport.RpcClient;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Inbound按照注册的先后顺序执行，Outbound按照注册的先后顺序逆序执行
 * Inbound由netty内部调用，Outbound由业务逻辑主动调用
 * 其中，Inbound用于处理request，outbound用于处理response
 *
 * @author Excelman
 * @date 2021/9/22 上午10:00
 * @description netty实现的客户端
 */
public class NettyClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private ServiceRegistry serviceRegistry;
    private CommonSerializer serializer;

    public NettyClient(){
        this(null, null);
    }
    public NettyClient(LoadBalancer loadBalancer, CommonSerializer serializer){
        if(null == loadBalancer){
            loadBalancer = new RandomLoadBalancer();
        }
        this.serviceRegistry = new NacosServiceRegistry(loadBalancer);
        if(null == serializer){
            this.serializer = CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER);
        }else{
            this.serializer = serializer;
        }
    }

    /**
     * 通过Netty发送RpcRequest请求，并得到RpcResponse返回结果
     * @param rpcRequest
     * @return
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try{
            /* 从RpcRequest中获取调用接口，从nacos注册器中获取该接口对应的InetSocket地址 */
            String interfaceName = rpcRequest.getInterfaceName();
            InetSocketAddress inetSocketAddress = serviceRegistry.lookupService(interfaceName);

            Channel channel = ChannelProvider.getChannel(inetSocketAddress, serializer);
            // 写入rpcRequest
            channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                if(future1.isSuccess()){
                    logger.info(String.format("客户端发送消息:%s", rpcRequest.toString()));
                }else{
                    logger.error("客户端发送消息失败:", future1.cause());
                    throw new RpcException(RpcError.NETTY_SEND_MESSAGE_ERROR);
                }
            });
            channel.closeFuture().sync(); // 这里阻塞等待channel关闭，再执行后续操作（因此NettyClientHandler先将rpcResponse方法哦channel.attr中）
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RpcResponse");
            RpcResponse rpcResponse = channel.attr(key).get();
            return rpcResponse.getData();
        } catch (InterruptedException e) {
            logger.error("NettyClient发送请求的时候发生异常：{}",e);
        }
        return null;
    }
}
