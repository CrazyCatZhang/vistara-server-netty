package com.catzhang.im.service.group.model.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetRoleInGroupResp {

    private Long groupMemberId;

    private String memberId;

    private Integer role;

    private Long speakDate;

}
