package com.catzhang.im.service.group.model.resp;

import com.catzhang.im.service.group.dao.GroupMemberEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class AddGroupMemberResp {

    private GroupMemberEntity groupMemberEntity;

}
