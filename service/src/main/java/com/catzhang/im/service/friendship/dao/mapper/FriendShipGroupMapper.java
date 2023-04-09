package com.catzhang.im.service.friendship.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catzhang.im.service.friendship.dao.FriendShipGroupEntity;
import com.catzhang.im.service.friendship.model.req.IsDeletedFriendShipGroupReq;
import com.catzhang.im.service.friendship.model.req.RecoveryFriendShipGroupReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author crazycatzhang
 */
@Mapper
public interface FriendShipGroupMapper extends BaseMapper<FriendShipGroupEntity> {

    @Update("update im_friendship_group set del_flag=0,update_time=#{updateTime} where from_id=#{fromId} and group_name=#{groupName} and app_id=#{appId}")
    int recovery(RecoveryFriendShipGroupReq req);

    @Select("select * from im_friendship_group where from_id=#{fromId} and group_name=#{groupName} and app_id=#{appId} and del_flag=1;")
    FriendShipGroupEntity isDeletedGroup(IsDeletedFriendShipGroupReq req);
}
