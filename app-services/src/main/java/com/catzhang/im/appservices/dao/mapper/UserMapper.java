package com.catzhang.im.appservices.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catzhang.im.appservices.dao.User;
import com.catzhang.im.appservices.model.req.SearchUserReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author crazycatzhang
 */
@Mapper
@Repository
public interface UserMapper extends BaseMapper<User> {


    @Select("<script>" +
            " select user_id from app_user  " +
            "<if test = 'searchType == 1'> " +
            " where mobile = #{keyWord} " +
            " </if>" +
            " <if test = 'searchType == 2'> " +
            "  where user_name = #{keyWord} " +
            " </if> " +
            " </script> ")
    public List<String> searchUser(SearchUserReq req);

}
