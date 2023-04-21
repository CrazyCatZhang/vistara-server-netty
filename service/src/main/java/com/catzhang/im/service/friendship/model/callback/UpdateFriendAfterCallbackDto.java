package com.catzhang.im.service.friendship.model.callback;

import com.catzhang.im.service.friendship.model.req.FriendDto;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class UpdateFriendAfterCallbackDto {

    private String fromId;

    private FriendDto toItem;

}
