package com.catzhang.im.appservices.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.catzhang.im.appservices.common.ResponseVO;
import com.catzhang.im.appservices.dao.User;
import com.catzhang.im.appservices.dao.mapper.UserMapper;
import com.catzhang.im.appservices.enums.ErrorCode;
import com.catzhang.im.appservices.exception.ApplicationException;
import com.catzhang.im.appservices.model.dto.ImUserDataDto;
import com.catzhang.im.appservices.model.req.RegisterReq;
import com.catzhang.im.appservices.model.req.SearchUserReq;
import com.catzhang.im.appservices.model.resp.ImportUserResp;
import com.catzhang.im.appservices.service.ImService;
import com.catzhang.im.appservices.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    ImService imService;

    @Override
    public ResponseVO<User> getUserByUserNameAndPassword(String userName, String password) {

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userName);
        wrapper.eq("password", password);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return ResponseVO.errorResponse(ErrorCode.USER_NOT_EXIST);
        }

        return ResponseVO.successResponse(user);
    }

    @Override
    public ResponseVO<User> getUserByMobile(String mobile) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("mobile", mobile);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return ResponseVO.errorResponse(ErrorCode.USER_NOT_EXIST);
        }
        return ResponseVO.successResponse(user);
    }

    @Override
    public ResponseVO<User> getUserByUserName(String userName) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userName);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return ResponseVO.errorResponse(ErrorCode.USER_NOT_EXIST);
        }
        return ResponseVO.successResponse(user);
    }

    @Override
    public ResponseVO<User> getUserById(Integer userId) {

        User user = userMapper.selectById(userId);
        if (user == null) {
            return ResponseVO.errorResponse(ErrorCode.USER_NOT_EXIST);
        }
        return ResponseVO.successResponse(user);

    }

    @Override
    @Transactional
    public ResponseVO<User> registerUser(RegisterReq req) {

        User user = new User();
        user.setCreateTime(System.currentTimeMillis());
        user.setPassword(req.getPassword());
        user.setUserName(req.getUserName());
        user.setUserId(req.getUserName());
        userMapper.insert(user);

        ArrayList<User> users = new ArrayList<>();
        users.add(user);
        ResponseVO responseVO = imService.importUser(users);
        if (responseVO.isOk()) {
            Object data = responseVO.getData();
            ObjectMapper objectMapper = new ObjectMapper();
            ImportUserResp importUserResp = objectMapper.convertValue(data, ImportUserResp.class);

            Set<String> successId = importUserResp.getSuccessIds();
            if (successId.contains(user.getUserId())) {
                return ResponseVO.successResponse(user);
            } else {
                throw new ApplicationException(ErrorCode.REGISTER_ERROR);
            }
        } else {
            throw new ApplicationException(responseVO.getCode(), responseVO.getMsg());
        }
    }

    @Override
    public ResponseVO<ImUserDataDto> searchUser(SearchUserReq req) {

        List<String> userIds = userMapper.searchUser(req);

        ResponseVO<ImUserDataDto> userInfo = imService.getUserInfo(userIds);

        return userInfo;
    }
}
