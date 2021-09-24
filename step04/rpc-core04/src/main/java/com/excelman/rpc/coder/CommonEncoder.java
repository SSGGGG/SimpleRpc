package com.excelman.rpc.coder;

import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.enumeration.PackageType;
import com.excelman.rpc.serializer.CommonSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Excelman
 * @date 2021/9/22 上午10:15
 * @description 统一编码拦截器，继承MessageToByteEncoder
 */
public class CommonEncoder extends MessageToByteEncoder {

    private final Logger logger = LoggerFactory.getLogger(CommonEncoder.class);

    // 魔数，表示一个协议包，服务端和客户端应该保持一致
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    private final CommonSerializer serializer;

    public CommonEncoder(){
        this(CommonSerializer.getByCode(CommonSerializer.DEFAULT_SERIALIZER));
    }

    public CommonEncoder(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 1. 写入4个字节的int类型的魔数
     * 2. 写入4个字节的int类型的包类型
     * 3. 写入4个字节的int类型的序列化器的code
     * 4. 写入4个字节的int类型的字节长度
     * 5. 写入序列化后的字节数组
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object in, ByteBuf out) throws Exception {
        out.writeInt(MAGIC_NUMBER);
        if(in instanceof RpcResponse){
            out.writeInt(PackageType.RESPONSE_TYPE.getCode());
        }else{
            out.writeInt(PackageType.REQUEST_TYPE.getCode());
        }
        out.writeInt(serializer.getCode());
        byte[] bytes = serializer.serialize(in);
        // 防止粘包的操作
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
