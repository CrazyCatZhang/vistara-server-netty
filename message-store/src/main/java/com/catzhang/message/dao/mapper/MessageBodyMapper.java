package com.catzhang.message.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catzhang.message.dao.MessageBodyEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author crazycatzhang
 */
@Repository
public interface MessageBodyMapper extends BaseMapper<MessageBodyEntity> {
}
