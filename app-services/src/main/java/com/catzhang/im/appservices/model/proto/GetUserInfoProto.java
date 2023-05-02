package com.catzhang.im.appservices.model.proto;

import lombok.Data;

import java.util.List;


/**
 * @author crazycatzhang
 */
@Data
public class GetUserInfoProto {

    private List<String> userIds;

    private List<String> standardField;

    private List<String> customField;

}
