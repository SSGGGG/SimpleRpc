package com.excelman.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.enumeration.RpcError;
import com.excelman.rpc.enumeration.SerializerType;
import com.excelman.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Excelman
 * @date 2021/9/22 上午10:26
 * @description kryo序列化器
 */
public class KryoSerializer implements CommonSerializer{

    private final Logger logger = LoggerFactory.getLogger(KryoSerializer.class);

    /**
     * todo 为什么设置threadLocal呢？？
     * get()??
     * remove()??
     */
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Output output = new Output(outputStream)){
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (IOException e) {
            logger.info("Kryo序列化过程中发生异常：{}", e);
            throw new RpcException(RpcError.KRYO_SERIALIZE_ERROR);
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try(ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            Input input  = new Input(inputStream)){
            Kryo kryo = kryoThreadLocal.get();
            Object object = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return object;
        } catch (Exception e){
            logger.info("kryo解序列化过程中发生异常:{}",e);
            throw new RpcException(RpcError.KRYO_DESERIALIZE_ERROR);
        }
    }

    /**
     * 通过SerializerType枚举类获取code
     */
    @Override
    public int getCode() {
        return SerializerType.valueOf("KRYO").getCode();
    }
}
