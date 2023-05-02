package com.catzhang.im.appservices.service;


import com.catzhang.im.appservices.common.ResponseVO;
import com.catzhang.im.appservices.dao.User;
import com.catzhang.im.appservices.model.dto.ImUserDataDto;
import com.catzhang.im.appservices.model.req.RegisterReq;
import com.catzhang.im.appservices.model.req.SearchUserReq;

/**
 * @author crazycatzhang
 */
public interface UserService {

    ResponseVO<User> getUserByUserNameAndPassword(String userName, String password);

    ResponseVO<User> getUserByMobile(String mobile);

    ResponseVO<User> getUserByUserName(String userName);

    ResponseVO<User> getUserById(Integer userId);

    ResponseVO<User> registerUser(RegisterReq req);

    ResponseVO<ImUserDataDto> searchUser(SearchUserReq req);

}
