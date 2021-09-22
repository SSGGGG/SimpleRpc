package com.excelman.rpc.serializer;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.enumeration.RpcError;
import com.excelman.rpc.enumeration.SerializerType;
import com.excelman.rpc.exception.RpcException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Excelman
 * @date 2021/9/22 下午2:16
 * @description JSON格式的序列化器
 */
public class JsonSerializer implements CommonSerializer{

    private final Logger logger = LoggerFactory.getLogger(JsonSerializer.class);

    // 使用Jackson工具
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) {
        try{
            byte[] bytes = objectMapper.writeValueAsBytes(obj);
            return bytes;
        } catch (JsonProcessingException e) {
            logger.info("json序列化器，在序列化过程中发生异常：{}",e);
            throw new RpcException(RpcError.JSON_SERIALIZE_ERROR);
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try{
            Object result = objectMapper.readValue(bytes, clazz);
            if(result instanceof RpcRequest){
                result = handleRpcRequest(result);
            }
            return result;
        } catch (IOException e){
            logger.info("json序列化器，在反序列化过程中发生异常：{}",e);
            throw new RpcException(RpcError.JSON_DESERIALIZE_ERROR);
        }
    }

    /**
     * 在JSON反序列化RpcRequest的过程中，由于存在一个Object[]字段，在反序列化中可能会失败（Object类型模糊），因此需要手动赋予类型
     *
     * 注：这种情况在其他的序列化方法中不会出现。因为JSON序列化本质上是将对象转换为JSON字符串，会丢失对象的类型信息。而其他的序列化方式本质上是转换为字节数组，因此会记录对象的类型信息
     */
    private Object handleRpcRequest(Object result) throws IOException {
        RpcRequest rpcRequest = (RpcRequest) result;
        for(int i=0; i<rpcRequest.getParameters().length; i++){
            Class<?> paramType = rpcRequest.getParamTypes()[i];
            if(!paramType.isAssignableFrom(rpcRequest.getParameters()[i].getClass())){
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequest.getParameters()[i]);
                rpcRequest.getParameters()[i] = objectMapper.readValue(bytes, paramType);
            }
        }
        return rpcRequest;
    }

    @Override
    public int getCode() {
        return SerializerType.valueOf("JSON").getCode();
    }
}
