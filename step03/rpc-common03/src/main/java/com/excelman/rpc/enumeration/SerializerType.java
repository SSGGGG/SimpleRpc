package com.excelman.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Excelman
 * @date 2021/9/22 上午10:50
 * @description 序列化类型
 */
@Getter
@AllArgsConstructor
public enum SerializerType {

    KRYO(0),
    JSON(1),
    HESSIAN(2),
    PROTOBUF(3);

    private final int code;
}
