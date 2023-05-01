package com.catzhang.im.service.user.service;

import com.catzhang.im.service.user.model.UserStatusChangeNotifyContent;

/**
 * @author crazycatzhang
 */
public interface UserStatusService {

    void processUserOnlineStatusChangeNotify(UserStatusChangeNotifyContent content);

}
