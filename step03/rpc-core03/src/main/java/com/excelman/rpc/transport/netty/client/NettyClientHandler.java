package com.excelman.rpc.transport.netty.client;

import com.excelman.rpc.entity.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            AttributeKey<Object> key = AttributeKey.valueOf("rpcResponse");
            ctx.channel().attr(key).set(rpcResponse);
            ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(rpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("ClientHandler处理RpcResponse时出现异常:");
        cause.printStackTrace();
    }
}
