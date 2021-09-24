package com.excelman.rpc.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Excelman
 * @date 2021/9/13 下午7:22
 * @description RPC的封装请求，以便服务方知道具体调用哪个接口的哪个方法，记得实现序列化接口
 */
@Data
@Getter
@Setter
public class RpcRequest implements Serializable {

    /**
     * 待调用的接口名称
     */
    private String interfaceName;

    /**
     * 待调用的方法名
     */
    private String methodName;

    /**
     * 调用的方法参数
     */
    private Object[] parameters;

    /**
     * 调用的参数类型
     */
    private Class<?>[] paramTypes;

    /**
     * 是否是心跳包
     */
    private Boolean isHeartBeat;
}
