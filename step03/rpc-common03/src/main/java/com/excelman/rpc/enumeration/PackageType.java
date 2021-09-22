package com.excelman.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.xml.ws.ServiceMode;

/**
 * @author Excelman
 * @date 2021/9/22 上午10:20
 * @description netty传输数据的包类型（request/response）
 */
@AllArgsConstructor
@Getter
public enum PackageType {

    REQUEST_TYPE(0),
    RESPONSE_TYPE(1)
    ;

    private final int code;
}
