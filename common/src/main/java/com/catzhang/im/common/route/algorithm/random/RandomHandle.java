package com.catzhang.im.common.route.algorithm.random;

import com.catzhang.im.common.enums.UserErrorCode;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author crazycatzhang
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
