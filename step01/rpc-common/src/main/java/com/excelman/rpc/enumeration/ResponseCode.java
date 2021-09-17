package com.excelman.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Excelman
 * @date 2021/9/13 下午7:30
 * @description 响应状态码
 */
@Getter
@AllArgsConstructor
public enum ResponseCode {
    SUCCESS(200, "调用方法成功"),
    FAIL(500, "调用方法失败"),
    ;

    private final int code;
    private final String message;
}