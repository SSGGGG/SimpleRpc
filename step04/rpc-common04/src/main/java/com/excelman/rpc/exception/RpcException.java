package com.excelman.rpc.exception;

import com.excelman.rpc.enumeration.RpcError;

/**
 * @author Excelman
 * @date 2021/9/14 上午11:25
 * @description 自定义异常类
 */
public class RpcException extends RuntimeException {

    public RpcException(RpcError error, String detail) {
        super(error.getMessage() + ": " + detail);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcError error) {
        super(error.getMessage());
    }

}