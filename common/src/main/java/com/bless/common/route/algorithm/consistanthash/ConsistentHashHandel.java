package com.bless.common.route.algorithm.consistanthash;

import com.bless.common.route.RouteHandle;

import java.util.List;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-26 17:11
 */

public class ConsistentHashHandel implements RouteHandle {

    //TreeMap
    private AbstractConsistentHash hash;
    public void setHash(AbstractConsistentHash hash) {
        this.hash = hash;
    }
    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values, key);
    }
}
