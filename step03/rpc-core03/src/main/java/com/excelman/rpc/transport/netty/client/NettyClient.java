package com.excelman.rpc.transport.netty.client;

import com.excelman.rpc.coder.CommonDecoder;
import com.excelman.rpc.coder.CommonEncoder;
import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.enumeration.RpcError;
import com.excelman.rpc.exception.RpcException;
import com.excelman.rpc.serializer.JsonSerializer;
import com.excelman.rpc.transport.RpcClient;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Excelman
 * @date 2021/9/22 上午10:00
 * @description netty实现的客户端
 */
public class NettyClient implements RpcClient {

    private final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private static final EventLoopGroup group;
    private static final Bootstrap bootstrap;

    /**
     * 静态初始化netty，在sendRequest方法中发送请求的时候再启动
     */
    static {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new CommonDecoder());
                        pipeline.addLast(new CommonEncoder(new JsonSerializer()));
                        pipeline.addLast(new NettyClientHandler()); // 责任链的尾部
                    }
                });
    }

    private String host;
    private int port;

    public NettyClient(String host, int port){
        this.host = host;
        this.port = port;
    }

    /**
     * 通过Netty发送RpcRequest请求，并得到RpcResponse返回结果
     * @param rpcRequest
     * @return
     */
    @Override
    public Object sendRequest(RpcRequest rpcRequest) {
        try{
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
                channel.closeFuture().sync();
                AttributeKey<RpcResponse> key = AttributeKey.valueOf("rpcResponse");
                RpcResponse rpcResponse = channel.attr(key).get();
                return rpcResponse.getData();
            }
        } catch (InterruptedException e) {
            logger.error("NettyClient发送请求的时候发生异常：{}",e);
        }
        return null;
    }
}
