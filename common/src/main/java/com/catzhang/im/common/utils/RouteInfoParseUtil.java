package com.catzhang.im.common.utils;

import com.catzhang.im.common.BaseErrorCode;
import com.catzhang.im.common.exception.ApplicationException;
import com.catzhang.im.common.route.RouteInfo;

/**
 * @author crazycatzhang
 */
public class RouteInfoParseUtil {

    public static RouteInfo parse(String info) {
        try {
            String[] serverInfo = info.split(":");
            RouteInfo routeInfo = new RouteInfo(serverInfo[0], Integer.parseInt(serverInfo[1]));
            return routeInfo;
        } catch (Exception e) {
            throw new ApplicationException(BaseErrorCode.PARAMETER_ERROR);
        }
    }

}
