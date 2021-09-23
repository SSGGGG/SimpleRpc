package com.excelman.rpc.transport.netty.client;

import com.excelman.rpc.coder.CommonDecoder;
import com.excelman.rpc.coder.CommonEncoder;
import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.provider.DefaultServiceProvider;
import com.excelman.rpc.provider.ServiceProvider;
import com.excelman.rpc.registry.NacosServiceRegistry;
import com.excelman.rpc.registry.ServiceRegistry;
import com.excelman.rpc.serializer.KryoSerializer;
import com.excelman.rpc.transport.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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

    private final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private static final EventLoopGroup group;
    private static final Bootstrap bootstrap;
    private static ServiceRegistry serviceRegistry;

    /**
     * 静态初始化netty，在sendRequest方法中发送请求的时候再启动
     * 其中handler的执行顺序
     *      接受请求时：Decoder----NettyClientHandler
     *      返回结果时：Encoder
     *
     * 静态创建ServiceRegistry对象
     */
    static {
        serviceRegistry = new NacosServiceRegistry();

        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // CommonDecoder是Inbound类型
                        pipeline.addLast(new CommonDecoder());
                        // CommonEncoder是Outbound类型
                        pipeline.addLast(new CommonEncoder(new KryoSerializer()));
                        // 自定义的handler是Inbound类型
                        pipeline.addLast(new NettyClientHandler());
                    }
                });
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
            String host = inetSocketAddress.getHostName();
            int port = inetSocketAddress.getPort();

            ChannelFuture future = bootstrap.connect(host, port).sync();
            logger.info("客户端连接到服务端host:{},port:{}", host, port);
            Channel channel = future.channel();
            if(channel != null){
                // 写入rpcRequest
                channel.writeAndFlush(rpcRequest).addListener(future1 -> {
                    if(future1.isSuccess()){
                        logger.info(String.format("客户端发送消息:%s", rpcRequest.toString()));
                    }else{
                        logger.error("客户端发送消息失败:", future1.cause());
                    }
                });
                channel.closeFuture().sync(); // 这里阻塞等待channel关闭，再执行后续操作（因此NettyClientHandler先将rpcResponse方法哦channel.attr中）
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("RpcResponse");
                RpcResponse rpcResponse = channel.attr(key).get();
                return rpcResponse.getData();
            }
        } catch (InterruptedException e) {
            logger.error("NettyClient发送请求的时候发生异常：{}",e);
        }
        return null;
    }
}
