package com.catzhang.im.service.user.service.impl;

import com.catzhang.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.catzhang.im.common.enums.command.UserEventCommand;
import com.catzhang.im.common.model.ClientInfo;
import com.catzhang.im.common.model.UserSession;
import com.catzhang.im.service.friendship.service.FriendShipService;
import com.catzhang.im.service.user.model.UserStatusChangeNotifyContent;
import com.catzhang.im.service.user.service.UserStatusService;
import com.catzhang.im.service.utils.MessageProducer;
import com.catzhang.im.service.utils.UserSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Service
public class UserStatusServiceImpl implements UserStatusService {

    @Autowired
    UserSessionUtils userSessionUtils;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    FriendShipService friendShipService;

    @Override
    public void processUserOnlineStatusChangeNotify(UserStatusChangeNotifyContent content) {
        List<UserSession> userSession = userSessionUtils.getUserSession(content.getAppId(), content.getUserId());
        UserStatusChangeNotifyPack userStatusChangeNotifyPack = new UserStatusChangeNotifyPack();
        BeanUtils.copyProperties(content, userStatusChangeNotifyPack);
        userStatusChangeNotifyPack.setClient(userSession);

        syncSender(userStatusChangeNotifyPack, content.getUserId(),
                content);

        dispatcher(userStatusChangeNotifyPack, content.getUserId(),
                content.getAppId());
    }

    private void syncSender(Object pack, String userId, ClientInfo clientInfo) {
        messageProducer.sendToUserExceptClient(userId,
                UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY_SYNC,
                pack, clientInfo);
    }

    private void dispatcher(Object pack, String userId, Integer appId) {
        List<String> allFriendId = friendShipService.getAllFriendId(userId, appId);
        for (String fid : allFriendId) {
            messageProducer.sendToUser(fid, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                    pack, appId);
        }
    }
}
