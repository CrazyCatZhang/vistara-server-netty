package com.catzhang.im.codec.pack.message;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author crazycatzhang
 */
@Data
@NoArgsConstructor
public class RecallMessageNotifyPack {

    private String fromId;

    private String toId;

    private Long messageKey;

    private Long messageSequence;
}
