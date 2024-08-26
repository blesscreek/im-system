package com.bless.common.route.algorithm.random;

import com.bless.common.enums.UserErrorCode;
import com.bless.common.exception.ApplicationException;
import com.bless.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-26 12:44
 */

public class RandomHandle implements RouteHandle {
    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        int i = ThreadLocalRandom.current().nextInt(size);
        return values.get(i);
    }
}
