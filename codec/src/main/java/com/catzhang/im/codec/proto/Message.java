package com.catzhang.im.codec.proto;

import lombok.Data;
import lombok.ToString;

/**
 * @author crazycatzhang
 */
@Data
@ToString
public class Message {

    private MessageHeader messageHeader;

    private Object messagePack;

}
