package com.catzhang.im.service.group.model.resp;

import com.catzhang.im.service.group.dao.GroupEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetJoinedGroupResp {

    private Integer totalCount;

    private List<GroupEntity> groupList;

}
