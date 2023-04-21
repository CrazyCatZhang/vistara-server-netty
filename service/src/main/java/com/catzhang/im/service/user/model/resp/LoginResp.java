package com.catzhang.im.service.user.model.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class LoginResp {

    private String ip;

    private Integer port;

}
