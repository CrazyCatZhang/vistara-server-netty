package com.catzhang.im.service.group.model.resp;

import com.catzhang.im.service.group.dao.GroupRequestEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class GetGroupRequestResp {

    private List<GroupRequestEntity> groupRequestEntityList;

}
