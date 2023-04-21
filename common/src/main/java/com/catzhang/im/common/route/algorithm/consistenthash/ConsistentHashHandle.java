package com.catzhang.im.common.route.algorithm.consistenthash;

import com.catzhang.im.common.route.RouteHandle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsistentHashHandle implements RouteHandle {

    private AbstractConsistentHash hash;

    @Override
    public String routeServer(List<String> values, String key) {
        return hash.process(values, key);
    }
}
