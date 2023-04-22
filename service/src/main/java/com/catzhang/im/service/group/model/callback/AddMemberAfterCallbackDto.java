package com.catzhang.im.service.group.model.callback;

import com.catzhang.im.service.group.model.resp.AddMemberResp;
import lombok.Data;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
public class AddMemberAfterCallbackDto {

    private String groupId;

    private Integer groupType;

    private String operator;

    private List<AddMemberResp> member;

}
