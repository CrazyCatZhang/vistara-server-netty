package com.catzhang.im.service.friendship.model.resp;

import com.catzhang.im.service.friendship.dao.FriendShipRequestEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class GetFriendShipRequestResp {
    private List<FriendShipRequestEntity> friendShipRequestEntityList;
}
