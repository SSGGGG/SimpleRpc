package com.excelman.rpc.serializer;

/**
 * @author Excelman
 * @date 2021/9/22 上午10:23
 * @description 通用的序列化器接口
 */
public interface CommonSerializer {

    int KRYO_SERIALIZER = 0;
    int JSON_SERIALIZER = 1;
    int DEFAULT_SERIALIZER = KRYO_SERIALIZER;

    static CommonSerializer getByCode(int code){
        switch (code){
            case KRYO_SERIALIZER:
                return new KryoSerializer();
            case JSON_SERIALIZER:
                return new JsonSerializer();
            default:
                return new KryoSerializer();
        }
    }

    byte[] serialize(Object obj);

    Object deserialize(byte[] bytes, Class<?> clazz);

    int getCode();

}
