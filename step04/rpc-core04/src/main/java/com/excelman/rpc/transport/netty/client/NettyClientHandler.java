package com.excelman.rpc.transport.netty.client;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.serializer.CommonSerializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * @author Excelman
 * @date 2021/9/22 下午3:11
 * @description Netty客户端的处理器
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     * 通道读取
     * 当Netty通道取到数据的时候调用该方法（即客户端通道读取到RpcResponse）
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        try{
            // 这里由于前面已经有解码器解码了，所以可以直接将数据读取并放到ctx中
            logger.info("客户端接收到消息：{}",rpcResponse.toString());
            UnProcessRequest.complete(rpcResponse);
        } finally {
            ReferenceCountUtil.release(rpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("ClientHandler处理RpcResponse时出现异常:{}", cause);
        ctx.close();
    }

    /**
     * 当Idle不满足条件的时候(达到超时时间，还未发送消息)，会触发该方法
     * 在这里，即达到超时时间还未发送消息，就会触发当前方法，此时需要发送心跳包给服务端，以保持长连接
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        System.out.println("client EventTriggered-----");

        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if(state == IdleState.WRITER_IDLE){
                logger.info("发送心跳包到目的地：[{}]", ctx.channel().remoteAddress());
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setIsHeartBeat(true);
//                Channel channel = ChannelProvider.getChannel((InetSocketAddress) ctx.channel().remoteAddress(), CommonSerializer.getByCode(CommonSerializer.KRYO_SERIALIZER));
//                channel.writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                ctx.channel().writeAndFlush(rpcRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }
}
