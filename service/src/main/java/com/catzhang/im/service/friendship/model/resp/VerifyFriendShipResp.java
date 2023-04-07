package com.catzhang.im.service.friendship.model.resp;

import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
public class VerifyFriendShipResp {
    private String fromId;

    private String toId;

    //校验状态，根据双向校验和单向校验有不同的status
    //单向校验：1 from添加了to，不确定to是否添加了from
    //        0  from没有添加to，也不确定to有没有添加from
    //双向校验 1 from添加了to，to也添加了from
    //        2 from添加了t0，to没有添加from
    //        3 from没有添加to，to添加了from
    //        4 双方都没有添加
    private Integer status;
    private String message;
}
