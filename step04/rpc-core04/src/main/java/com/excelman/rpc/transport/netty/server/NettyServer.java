package com.excelman.rpc.transport.netty.server;

import com.excelman.rpc.coder.CommonDecoder;
import com.excelman.rpc.coder.CommonEncoder;
import com.excelman.rpc.provider.DefaultServiceProvider;
import com.excelman.rpc.provider.ServiceProvider;
import com.excelman.rpc.registry.NacosServiceRegistry;
import com.excelman.rpc.registry.ServiceRegistry;
import com.excelman.rpc.serializer.KryoSerializer;
import com.excelman.rpc.transport.RpcServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Inbound按照注册的先后顺序执行，Outbound按照注册的先后顺序逆序执行
 * Inbound由netty内部调用，outbound由业务逻辑主动调用
 * 其中，Inbound用于处理request，outbound用于处理response
 *
 * @author Excelman
 * @date 2021/9/22 上午10:00
 * @description netty实现的服务端
 */
public class NettyServer implements RpcServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    private final String host;
    private final int port;
    private ServiceRegistry serviceRegistry;
    private ServiceProvider serviceProvider;

    public NettyServer(String host, int port){
        this.host = host;
        this.port = port;
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new DefaultServiceProvider();
    }

    /**
     * handler的执行顺序
     *      接受请求时：IdleState----Decoder----NettyServerHandler
     *      返回结果时：Encoder
     */
    @Override
    public void start() {
        // parentGroup，负责处理TCP/IP连接
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // childGroup，负责处理Channel的IO处理
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 设置日志处理器以及option
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 设置其它的handler，其中每一个handler都是有一个线程去执行
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // IdleStateHandler属于Inbound类型
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new CommonEncoder(new KryoSerializer())) // out类型
                                    .addLast(new CommonDecoder())   // In类型
                                    .addLast(new NettyServerHandler()); // In类型
                        }
                    });
            // 绑定监听端口，调用sync同步阻塞方法等待绑定执行结束
            ChannelFuture future = serverBootstrap.bind(this.port).sync();
            // 成功绑定到端口之后,给channel增加一个 管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束进程
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            logger.info("NettyServer在启动的时候发生异常：{}",e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 将service及其实现的接口注册到本地Provider的map中，再将具体实现类及其地址注册到NacosRegistry中
     * (客户端欲获取地址的时候，首先通过本地Provider获取接口对应的具体实现类，再从NacosRegistry中获取地址)
     *
     * @param service 具体服务实现类
     * @param serviceClass 服务的接口类型
     * @param <T>
     */
    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {
        // 将服务保存在本地map中
        serviceProvider.registerProvider(service);
        // 将接口服务及其地址保存在nacos中
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
    }
}
