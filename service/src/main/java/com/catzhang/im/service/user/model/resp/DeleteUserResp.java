package com.catzhang.im.service.user.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
public class DeleteUserResp {

    private List<String> successIds;

    private List<String> errorIds;
}
