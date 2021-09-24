package com.excelman.rpc.server;

import com.excelman.rpc.annotation.ServiceScan;
import com.excelman.rpc.transport.RpcServer;
import com.excelman.rpc.transport.socket.server.SocketServer;

/**
 * @author Excelman
 * @date 2021/9/24 上午9:55
 * @description Socket服务端测试
 */
@ServiceScan
public class SocketServerTest {

    public static void main(String[] args) {
        // 1. 创建SocketServer
        RpcServer server = new SocketServer("localhost", 9500);

        // 2. start
        server.start();
    }
}
