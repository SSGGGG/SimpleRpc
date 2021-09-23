package com.excelman.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 * @author Excelman
 * @date 2021/9/23 下午3:18
 * @description
 */
public class RandomLoadBalancer implements LoadBalancer{

    private static final Logger logger = LoggerFactory.getLogger(RandomLoadBalancer.class);

    /**
     * by random strategy
     * @param instanceList
     * @return
     */
    @Override
    public Instance select(List<Instance> instanceList) {
        return instanceList.get(new Random().nextInt(instanceList.size()));
    }
}
