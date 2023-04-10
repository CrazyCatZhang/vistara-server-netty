package com.catzhang.im.service.group.model.resp;

import com.catzhang.im.service.group.dao.GroupMemberEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class CreateGroupResp {

    private Map<String, List<GroupMemberEntity>> groupInfo;

    private List<String> failureMembers;

}
