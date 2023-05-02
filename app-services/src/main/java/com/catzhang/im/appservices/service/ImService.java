package com.catzhang.im.appservices.service;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.appservices.common.ResponseVO;
import com.catzhang.im.appservices.config.AppConfig;
import com.catzhang.im.appservices.dao.User;
import com.catzhang.im.appservices.model.dto.ImUserDataDto;
import com.catzhang.im.appservices.model.proto.GetUserInfoProto;
import com.catzhang.im.appservices.model.proto.ImportUserProto;
import com.catzhang.im.appservices.model.resp.ImportUserResp;
import com.catzhang.im.appservices.utils.HttpRequestUtils;
import com.catzhang.im.appservices.utils.SignApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author crazycatzhang
 */
@Service
public class ImService implements CommandLineRunner {

    @Autowired
    HttpRequestUtils httpRequestUtils;

    @Autowired
    AppConfig appConfig;

    private SignApi signApi;

    public volatile static Map<String, Object> parameter;

    public static final Object lock = new Object();

    private String getUrl(String uri) {
        return appConfig.getImUrl() + "/" + uri;
    }

    private Map<String, Object> getParameter() {
        if (parameter == null) {
            synchronized (lock) {
                if (parameter == null) {
                    parameter = new ConcurrentHashMap<>();
                    parameter.put("appId", appConfig.getAppId());
                    parameter.put("userSign", signApi.genUserSign(appConfig.getAdminId(), 500000));
                    parameter.put("identifier", appConfig.getAdminId());
                }
            }
        }
        return parameter;
    }

    public ResponseVO<ImportUserResp> importUser(List<User> users) {

        ImportUserProto proto = new ImportUserProto();
        List<ImportUserProto.UserData> userData = new ArrayList<>();
        users.forEach(e -> {
            ImportUserProto.UserData u = new ImportUserProto.UserData();
            u.setUserId(e.getUserId());
            u.setPassword(e.getPassword());
            u.setUserType(1);
            userData.add(u);
        });

        String uri = "/user/import";
        try {
            proto.setUserData(userData);
            ResponseVO responseVO = httpRequestUtils.doPost(getUrl(uri), ResponseVO.class, getParameter(), null, JSONObject.toJSONString(proto), "");
            return responseVO;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseVO.errorResponse();
    }


    public ResponseVO<ImUserDataDto> getUserInfo(List<String> users) {

        GetUserInfoProto proto = new GetUserInfoProto();
        proto.setUserIds(users);

        String uri = "/user/getUserInfo";
        try {
//            proto.setUserData(userData);
            ResponseVO responseVO = httpRequestUtils.doPost(getUrl(uri), ResponseVO.class, getParameter(), null, JSONObject.toJSONString(proto), "");
            return responseVO;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseVO.errorResponse();
    }


    @Override
    public void run(String... args) throws Exception {
        signApi = new SignApi(appConfig.getAppId(), appConfig.getPrivateKey());
    }
}
