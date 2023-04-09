package com.catzhang.im.service.friendship.model.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class AddFriendShipGroupMemberResp {
    private List<String> successIds;
}
