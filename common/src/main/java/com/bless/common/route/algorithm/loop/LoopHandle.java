package com.bless.common.route.algorithm.loop;

import com.bless.common.enums.UserErrorCode;
import com.bless.common.exception.ApplicationException;
import com.bless.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-26 16:50
 */

public class LoopHandle  implements RouteHandle {
    private AtomicLong index = new AtomicLong();
    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        Long l = index.incrementAndGet() % size;
        if (l < 0) {
            l = 0L;
        }
        return values.get(l.intValue());

    }
}
