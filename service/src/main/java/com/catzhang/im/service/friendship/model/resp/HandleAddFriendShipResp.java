package com.catzhang.im.service.friendship.model.resp;

import com.catzhang.im.service.friendship.dao.FriendShipEntity;
import lombok.Data;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
public class HandleAddFriendShipResp {
    private List<FriendShipEntity> friendShipEntities;
}
