package com.catzhang.message.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catzhang.message.dao.MessageHistoryEntity;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * @author crazycatzhang
 */
@Repository
public interface MessageHistoryMapper extends BaseMapper<MessageHistoryEntity> {

    /**
     * 批量插入（mysql）
     *
     * @param entityList
     * @return
     */
    Integer insertBatchSomeColumn(Collection<MessageHistoryEntity> entityList);
}
