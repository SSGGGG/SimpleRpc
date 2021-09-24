package com.excelman.rpc.coder;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.enumeration.PackageType;
import com.excelman.rpc.enumeration.RpcError;
import com.excelman.rpc.exception.RpcException;
import com.excelman.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Excelman
 * @date 2021/9/22 上午10:14
 * @description 自定义统一解码拦截器，继承ReplayingDecoder
 */
public class CommonDecoder extends ReplayingDecoder {

    private final Logger logger = LoggerFactory.getLogger(CommonDecoder.class);

    // 魔数，表示一个协议包，服务端和客户端应该保持一致
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    /**
     * 1. 识别协议包(MAGIC_NUMBER)，并判断真伪
     * 2. 识别packageType，用于反序列化
     * 3. 识别反序列化器类型
     * 4. 通过反序列化器，反序列化数据，并添加到out中
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> out) throws Exception {
        int magic = in.readInt();
        if(magic != MAGIC_NUMBER){
            logger.info("不识别的协议包：{}",magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }
        int packageType = in.readInt();
        Class<?> packageClass;
        if(packageType == PackageType.REQUEST_TYPE.getCode()){
            packageClass = RpcRequest.class;
        }else if(packageType == PackageType.RESPONSE_TYPE.getCode()){
            packageClass = RpcResponse.class;
        }else{
            logger.info("不识别的包类型：{}", packageType);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }
        // 最终进行反序列化过程
        int serializerType = in.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerType);
        if(null == serializer){
            logger.info("序列化类型{}不存在",serializerType);
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        // 防止粘包的操作
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object result = serializer.deserialize(bytes, packageClass);
        out.add(result);
    }
}
