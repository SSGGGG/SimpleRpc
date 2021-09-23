package com.excelman.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * @author Excelman
 * @date 2021/9/23 下午3:17
 */
public interface LoadBalancer {

    /**
     * Select instance from instance list by some strategy
     * @param instanceList
     * @return
     */
    Instance select(List<Instance> instanceList);
}
