package com.excelman.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Excelman
 * @date 2021/9/23 下午3:22
 * @description
 */
public class RoundRobinLoadBalancer implements LoadBalancer{

    private static final Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);

    // 选择服务器的序号
    private static int index = 0;

    /**
     * by round robin strategy
     * @param instanceList
     * @return
     */
    @Override
    public Instance select(List<Instance> instanceList) {
        if(index >= instanceList.size()){
            index  %= instanceList.size();
        }
        return instanceList.get(index++);
    }
}
