package com.catzhang.im.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;


/**
 * @author crazycatzhang
 */
@Data
@Component
@ConfigurationProperties(prefix = "appconfig")
public class AppConfig {

    /** appId */
    private Integer appId;

    private String privateKey;

    /** zk连接地址*/
    private String zkAddr;

    /** zk连接超时时间*/
    private Integer zkConnectTimeOut;

    /** im管道地址路由策略*/
    private Integer imRouteWay;

    private boolean sendMessageCheckFriend; //发送消息是否校验关系链

    private boolean sendMessageCheckBlack; //发送消息是否校验黑名单

    /** 如果选用一致性hash的话具体hash算法*/
    private Integer consistentHashWay;

    private Integer tcpPort; //tcp 端口

    private Integer webSocketPort; //webSocket端口

    private boolean needWebSocket; //是否需要开启WebSocket

    private Integer loginModel; //登录模式

    private Integer messageRecallTimeOut; //消息可撤回时间

    private Integer groupMaxMemberCount; // 群组最大成员数量

    private String callbackUrl; //应用服务回调地址

    private boolean modifyUserAfterCallback; //用户资料变更之后回调开关

    private boolean addFriendAfterCallback; //添加好友之后回调开关

    private boolean addFriendBeforeCallback; //添加好友之前回调开关

    private boolean modifyFriendAfterCallback; //修改好友之后回调开关

    private boolean deleteFriendAfterCallback; //删除好友之后回调开关

    private boolean addFriendShipBlackAfterCallback; //添加黑名单之后回调开关

    private boolean deleteFriendShipBlackAfterCallback; //删除黑名单之后回调开关

    private boolean createGroupAfterCallback; //创建群聊之后回调开关

    private boolean modifyGroupAfterCallback; //修改群聊之后回调开关

    private boolean destroyGroupAfterCallback;//解散群聊之后回调开关

    private boolean deleteGroupMemberAfterCallback;//删除群成员之后回调

    private boolean addGroupMemberBeforeCallback;//拉人入群之前回调

    private boolean addGroupMemberAfterCallback;//拉人入群之后回调

    private boolean sendMessageAfterCallback;//发送单聊消息之后

    private boolean sendMessageBeforeCallback;//发送单聊消息之前

    private Integer deleteConversationSyncMode; //多端同步

    private Integer offlineMessageCount;//离线消息最大条数

    private boolean sendGroupMessageAfterCallback; //发送群聊消息之后

    private boolean sendGroupMessageBeforeCallback; // 发送群里消息之前

}
