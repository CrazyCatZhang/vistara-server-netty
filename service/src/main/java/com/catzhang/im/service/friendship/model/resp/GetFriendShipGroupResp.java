package com.catzhang.im.service.friendship.model.resp;

import com.catzhang.im.service.friendship.dao.FriendShipGroupEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class GetFriendShipGroupResp {
    private FriendShipGroupEntity friendShipGroupEntity;
}
