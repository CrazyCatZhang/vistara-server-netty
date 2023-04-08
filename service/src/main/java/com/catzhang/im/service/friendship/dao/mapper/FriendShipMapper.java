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
    List<VerifyFriendShipResp> verifyFriendShip(VerifyFriendShipReq req);

    @Select("<script>" +
            " select a.fromId,a.toId , ( \n" +
            " case \n" +
            " when a.status = 1 and b.status = 1 then 1 \n" +
            " when a.status = 1 and b.status != 1 then 2 \n" +
            " when a.status != 1 and b.status = 1 then 3 \n" +
            " when a.status != 1 and b.status != 1 then 4 \n" +
            " end \n" +
            " ) \n " +
            " as status from " +
            " (select from_id AS fromId , to_id AS toId , if(status = 1,1,0) as status from im_friendship where app_id = #{appId} and from_id = #{fromId} AND to_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " ) as a INNER join" +
            " (select from_id AS fromId, to_id AS toId , if(status = 1,1,0) as status from im_friendship where app_id = #{appId} and to_id = #{fromId} AND from_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " ) as b " +
            " on a.fromId = b.toId AND b.fromId = a.toId " +
            "</script>"
    )
    List<VerifyFriendShipResp> verifyBidirectionalFriendShip(VerifyFriendShipReq req);


    @Select("<script>" +
            " select from_id AS fromId, to_id AS toId , if(black = 1,1,0) as status from im_friendship where app_id = #{appId} and from_id = #{fromId}  and  to_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            "</script>"
    )
    List<VerifyFriendShipResp> verifyFriendShipBlack(VerifyFriendShipReq req);

    @Select("<script>" +
            " select a.fromId,a.toId , ( \n" +
            " case \n" +
            " when a.black = 1 and b.black = 1 then 1 \n" +
            " when a.black = 1 and b.black != 1 then 2 \n" +
            " when a.black != 1 and b.black = 1 then 3 \n" +
            " when a.black != 1 and b.black != 1 then 4 \n" +
            " end \n" +
            " ) \n " +
            " as status from "+
            " (select from_id AS fromId , to_id AS toId , if(black = 1,1,0) as black from im_friendship where app_id = #{appId} and from_id = #{fromId} AND to_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " ) as a INNER join" +
            " (select from_id AS fromId, to_id AS toId , if(black = 1,1,0) as black from im_friendship where app_id = #{appId} and to_id = #{fromId} AND from_id in " +
            "<foreach collection='toIds' index='index' item='id' separator=',' close=')' open='('>" +
            " #{id} " +
            "</foreach>" +
            " ) as b " +
            " on a.fromId = b.toId AND b.fromId = a.toId "+
            "</script>"
    )
    List<VerifyFriendShipResp> verifyBidirectionalFriendShipBlack(VerifyFriendShipReq req);
}
