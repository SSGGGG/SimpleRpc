package com.excelman.rpc.server;

import com.excelman.rpc.annotation.ServiceScan;
import com.excelman.rpc.serializer.CommonSerializer;
import com.excelman.rpc.transport.RpcServer;
import com.excelman.rpc.transport.netty.server.NettyServer;

/**
 * @author Excelman
 * @date 2021/9/23 上午10:50
 * @description Netty服务端测试
 */
@ServiceScan
public class NettyServerTest {

    // NOW: server select kryo serializer

    public static void main(String[] args) {
        // 1. create NettyServer ( determine the host and port )
        RpcServer rpcServer = new NettyServer("localhost", 9500, CommonSerializer.getByCode(CommonSerializer.KRYO_SERIALIZER));

        // 2. start() the server
        rpcServer.start();
    }
}
