package com.catzhang.im.appservices.model.resp;

import lombok.Data;

import java.util.Set;


/**
 * @author crazycatzhang
 */
@Data
public class ImportUserResp {
    private Set<String> successIds;

    private Set<String> errorIds;
}
