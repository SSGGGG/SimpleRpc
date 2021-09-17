package com.excelman.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Excelman
 * @date 2021/9/13 下午7:12
 * @description rpc调用传输的对象，注意该类需要实现序列化接口
 */
@Data
@AllArgsConstructor
public class HelloObject implements Serializable {

    private Integer id;

    private String message;
}
