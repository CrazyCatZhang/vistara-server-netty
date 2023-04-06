package com.catzhang.im.service.friendship.model.req;

import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class FriendDto {
    private String toId;

    private String remark;

    private String addSource;

    private String extra;

    private String addWording;
}
