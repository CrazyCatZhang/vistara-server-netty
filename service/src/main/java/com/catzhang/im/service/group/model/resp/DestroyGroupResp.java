package com.catzhang.im.service.group.model.resp;

import com.catzhang.im.service.group.dao.GroupEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class DestroyGroupResp {

    private GroupEntity destroyedGroup;

}
