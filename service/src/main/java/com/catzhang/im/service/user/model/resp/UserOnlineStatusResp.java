package com.catzhang.im.service.user.model.resp;

import com.catzhang.im.common.model.UserSession;
import lombok.Data;

import java.util.List;


/**
 * @author crazycatzhang
 */
@Data
public class UserOnlineStatusResp {

    private List<UserSession> session;

    private String customText;

    private Integer customStatus;

}
