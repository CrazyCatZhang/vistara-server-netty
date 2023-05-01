package com.catzhang.im.common.model;

import lombok.Data;

import java.util.List;


/**
 * @author crazycatzhang
 */
@Data
public class SyncResp<T> {

    private Long maxSequence;

    private boolean isCompleted;

    private List<T> dataList;

}
