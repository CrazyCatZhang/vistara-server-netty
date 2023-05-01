package com.catzhang.im.codec.pack.user;

import lombok.Data;


/**
 * @author crazycatzhang
 */
@Data
public class UserCustomStatusChangeNotifyPack {

    private String customText;

    private Integer customStatus;

    private String userId;

}
