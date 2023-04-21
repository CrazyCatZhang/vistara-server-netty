package com.catzhang.im.common.route.algorithm.loop;

import com.catzhang.im.common.enums.UserErrorCode;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author crazycatzhang
 */
public class LoopHandle implements RouteHandle {

    private final AtomicLong index = new AtomicLong();

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        long l = index.incrementAndGet() % size;
        if (l < 0) {
            l = 0L;
        }
        return values.get((int) l);
    }
}
