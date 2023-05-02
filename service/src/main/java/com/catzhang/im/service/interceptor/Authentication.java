package com.catzhang.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.common.BaseErrorCode;
import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.config.AppConfig;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.GateWayErrorCode;
import com.catzhang.im.common.enums.UserType;
import com.catzhang.im.common.exception.ApplicationExceptionEnum;
import com.catzhang.im.common.utils.SignApi;
import com.catzhang.im.service.user.model.req.GetSingleUserInfoReq;
import com.catzhang.im.service.user.model.resp.GetSingleUserInfoResp;
import com.catzhang.im.service.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author crazycatzhang
 */
@Component
public class Authentication {

    private static Logger logger = LoggerFactory.getLogger(Authentication.class);

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    AppConfig appConfig;

    @Autowired
    UserService userService;

    public ApplicationExceptionEnum verifyUserSign(String appId, String identifier, String userSign) {

        String key = appId + ":" + Constants.RedisConstants.USERSIGN + ":" + identifier + userSign;
        String cacheUserSign = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isBlank(cacheUserSign) && Long.parseLong(cacheUserSign) > System.currentTimeMillis() / 1000) {
            return BaseErrorCode.SUCCESS;
        }

        //获取秘钥
        String privateKey = appConfig.getPrivateKey();

        //创建signApi
        SignApi signApi = new SignApi(Long.parseLong(appId), privateKey);

        //对userSign解密
        JSONObject jsonObject = signApi.decodeUserSign(userSign);

        //取出解密后的appid 和 操作人 和 过期时间做匹配，不通过则提示错误
        Long expireTime = 0L;
        Long expireSec = 0L;
        Long time = 0L;
        String decoderAppId = "";
        String decoderIdentifier = "";

        try {
            decoderAppId = jsonObject.getString("TLS.appId");
            decoderIdentifier = jsonObject.getString("TLS.identifier");
            String expireStr = jsonObject.get("TLS.expire").toString();
            String expireTimeStr = jsonObject.get("TLS.expireTime").toString();
            time = Long.valueOf(expireTimeStr);
            expireSec = Long.valueOf(expireStr);
            expireTime = time + expireSec;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("checkUserSig-error:{}", e.getMessage());
        }

        if (!decoderIdentifier.equals(identifier)) {
            return GateWayErrorCode.USER_SIGN_OPERATE_NOT_MATE;
        }

        if (!decoderAppId.equals(appId)) {
            return GateWayErrorCode.USER_SIGN_IS_ERROR;
        }

        if (expireSec == 0L) {
            return GateWayErrorCode.USER_SIGN_IS_EXPIRED;
        }

        if (expireTime < System.currentTimeMillis() / 1000) {
            return GateWayErrorCode.USER_SIGN_IS_EXPIRED;
        }

        String genSig = signApi.genUserSign(identifier, expireSec, time, null);
        if (genSig.toLowerCase().equals(userSign.toLowerCase())) {

            long etime = expireTime - System.currentTimeMillis() / 1000;
            stringRedisTemplate.opsForValue().set(
                    key, expireTime.toString(), etime, TimeUnit.SECONDS
            );
            this.setIsAdmin(identifier, Integer.valueOf(appId));
            return BaseErrorCode.SUCCESS;
        }

        return GateWayErrorCode.USER_SIGN_IS_ERROR;

    }

    private void setIsAdmin(String identifier, Integer appId) {
        GetSingleUserInfoReq getSingleUserInfoReq = new GetSingleUserInfoReq();
        getSingleUserInfoReq.setUserId(identifier);
        getSingleUserInfoReq.setAppId(appId);
        ResponseVO<GetSingleUserInfoResp> singleUserInfo = userService.getSingleUserInfo(getSingleUserInfoReq);
        if (singleUserInfo.isOk()) {
            RequestHolder.set(singleUserInfo.getData().getUserDataEntity().getUserType() == UserType.APP_ADMIN.getCode());
        } else {
            RequestHolder.set(false);
        }
    }

}
