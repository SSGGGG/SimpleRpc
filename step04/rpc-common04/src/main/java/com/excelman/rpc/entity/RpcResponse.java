package com.excelman.rpc.entity;

import com.excelman.rpc.enumeration.ResponseCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Excelman
 * @date 2021/9/13 下午7:25
 * @description RPC请求的响应封装类
 */
@Data
public class RpcResponse<T> implements Serializable {

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应体
     */
    private T data;

    /**
     * 成功的时候调用
     */
    public static <T> RpcResponse<T> success(T data){
        RpcResponse<T> response = new RpcResponse<T>();
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    /**
     * 失败的时候调用
     */
    public static <T> RpcResponse<T> fail(T data){
        RpcResponse<T> response = new RpcResponse<T>();
        response.setStatusCode(ResponseCode.FAIL.getCode());
        response.setData(data);
        return response;
    }
}
