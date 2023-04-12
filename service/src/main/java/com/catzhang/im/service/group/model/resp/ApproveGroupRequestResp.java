package com.catzhang.im.service.group.model.resp;

import com.catzhang.im.service.group.dao.GroupMemberEntity;
import com.catzhang.im.service.group.dao.GroupRequestEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApproveGroupRequestResp {

    private GroupRequestEntity groupRequest;

    private GroupMemberEntity groupMember;

}
