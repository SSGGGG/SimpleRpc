package com.excelman.rpc.transport.netty.client;

import com.excelman.rpc.entity.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Excelman
 * @date 2021/9/26 下午12:14
 * @description to save unprocess request, use completableFuture to complete async
 */
public class UnProcessRequest {

    private static final Logger logger = LoggerFactory.getLogger(UnProcessRequest.class);

    /* key:requestId, value:completableFuture to save response */
    private static final ConcurrentHashMap<String, CompletableFuture<RpcResponse>> futures = new ConcurrentHashMap<>(   );

    /**
     * put new future to futures map
     * @param requestId 
     */
    public static void put(String requestId, CompletableFuture<RpcResponse> future){
        futures.put(requestId, future);
    }

    /**
     * remove the element in futures map
     * @param requestId
     */
    public static void remove(String requestId){
        futures.remove(requestId);
    }

    /**
     * use when complete task, which complete future
     * @param response
     */
    public static void complete(RpcResponse response){
        try{
            CompletableFuture<RpcResponse> future = futures.remove(response.getRequestId());
            if(future != null)  future.complete(response);
            else throw new IllegalStateException();
        } catch (Exception e){
            logger.error("complete future occurs exception:{}",e);
        }
    }
}
