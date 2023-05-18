package com.catzhang.im.appservices.model.proto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class MessageBody {

    private Integer type;

    private String content;

}
