package com.catzhang.im.common.route.algorithm.consistenthash;

import com.catzhang.im.common.route.RouteHandle;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class ConsistentHashHandle implements RouteHandle {

    private AbstractConsistentHash abstractConsistentHash;

    @Override
    public String routeServer(List<String> values, String key) {
        return abstractConsistentHash.process(values, key);
    }
}
