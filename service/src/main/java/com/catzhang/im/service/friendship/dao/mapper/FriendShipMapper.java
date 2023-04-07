package com.catzhang.im.service.friendship.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catzhang.im.service.friendship.dao.FriendShipEntity;
import com.catzhang.im.service.friendship.model.req.VerifyFriendShipReq;
import com.catzhang.im.service.friendship.model.resp.VerifyFriendShipResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Mapper
public interface FriendShipMapper extends BaseMapper<FriendShipEntity> {
    @Select("<script>"
            + "select from_id as fromId , to_id as toId ,if(status = 1,1,0) as status from im_friendship where from_id = #{fromId} and to_id in "
            + "<foreach collection='toIds' index='index' item='id' separator=',' close = ')' open='(' > "
            + "#{id}"
            + "</foreach>"
            + "</script>")
    List<VerifyFriendShipResp> checkFriendShip(VerifyFriendShipReq req);
}
