package com.catzhang.im.service.friendship.model.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class GetAllFriendShipGroupResp {

    private Map<String, List<String>> groups;

}
