package com.catzhang.im.service.friendship.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
public class ImportFriendShipResp {
    private List<String> successIds;

    private List<String> errorIds;
}
