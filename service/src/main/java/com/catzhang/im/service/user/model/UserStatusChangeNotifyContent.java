package com.catzhang.im.service.user.model;

import com.catzhang.im.common.model.ClientInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description: status区分是上线还是下线
 * @author: lld
 * @version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserStatusChangeNotifyContent extends ClientInfo {


    private String userId;

    //服务端状态 1上线 2离线
    private Integer status;


}
