package com.catzhang.im.codec.pack.user;

import com.catzhang.im.common.model.UserSession;
import lombok.Data;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
public class UserStatusChangeNotifyPack {

    private Integer appId;

    private String userId;

    private Integer status;

    private List<UserSession> client;

}
