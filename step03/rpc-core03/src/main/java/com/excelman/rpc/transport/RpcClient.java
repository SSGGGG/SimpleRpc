package com.excelman.rpc.transport;

import com.excelman.rpc.entity.RpcRequest;

/**
 * @author Excelman
 * @date 2021/9/17 下午7:37
 * @description RpcClient的抽象接口定义
 */
public interface RpcClient {

    Object sendRequest(RpcRequest rpcRequest);
}
