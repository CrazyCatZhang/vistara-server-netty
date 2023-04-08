package com.catzhang.im.service.friendship.model.resp;

import com.catzhang.im.service.friendship.dao.FriendShipRequestEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class AddFriendShipRequestResp {
    private FriendShipRequestEntity friendShipRequestEntity;
}
