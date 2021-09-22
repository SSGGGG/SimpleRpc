package com.excelman.rpc.handler;

import com.excelman.rpc.entity.RpcRequest;
import com.excelman.rpc.entity.RpcResponse;
import com.excelman.rpc.enumeration.ResponseCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Excelman
 * @date 2021/9/22 下午3:55
 * @description 处理请求的具体实现类，职责：反射调用指定的类.方法()，并返回结果
 */
public class RequestHandler {

    /**
     * 执行反射方法，返回结果
     * @param service 具体服务类
     * @return
     */
    public Object handle(RpcRequest rpcRequest, Object service){
        Object result = null;
        try{
            result = handleInvoke(rpcRequest, service);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Object handleInvoke(RpcRequest rpcRequest, Object service) throws InvocationTargetException, IllegalAccessException {
        Method method = null;
        try{
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NO_FOUND);
        }
        return method.invoke(service, rpcRequest.getParameters());
    }

}
