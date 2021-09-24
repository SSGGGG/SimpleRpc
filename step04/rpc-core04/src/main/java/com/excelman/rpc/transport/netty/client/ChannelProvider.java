package com.excelman.rpc.transport.netty.client;

import com.excelman.rpc.coder.CommonDecoder;
import com.excelman.rpc.coder.CommonEncoder;
import com.excelman.rpc.serializer.CommonSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Excelman
 * @date 2021/9/24 下午7:40
 * @description Channel保存和获取,其中channel的关联key是接口服务在nacos中的地址
 */
public class ChannelProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(ChannelProvider.class);

    private static EventLoopGroup group;
    private static Bootstrap bootstrap;

    static{
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }

    /* key为socketAddress+serializer.getCode(), value为Channel */
    private static final Map<String, Channel> channels = new HashMap<>();

    public static Channel getChannel(InetSocketAddress address, CommonSerializer serializer){
        String key = address.toString() + serializer.getCode();
        if(channels.containsKey(key)){
            Channel channel = channels.get(key);
            if(channel.isActive()) return channel;
            else{
                channels.remove(key);
            }
        }
        /*
         * 其中handler的执行顺序
         *      接受请求时：Idle----Decoder----NettyClientHandler
         *      返回结果时：Encoder
         */
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IdleStateHandler(0, 2, 0, TimeUnit.SECONDS))
                        .addLast(new CommonDecoder())
                        .addLast(new CommonEncoder(serializer))
                        .addLast(new NettyClientHandler());
            }
        });
        Channel channel;
        try{
            channel = connect(address);
        } catch (Exception e){
            logger.error("client connect occurs exception:{}",e);
            return null;
        }
        channels.put(key, channel);
        return channel;
    }

    /**
     * async connect
     * @param address
     * @return
     */
    private static Channel connect(InetSocketAddress address) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(address.getHostName(), address.getPort()).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                logger.info("client connect success!!!");
                completableFuture.complete(future.channel());
            }else{
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }
}
