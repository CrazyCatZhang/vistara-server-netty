package com.catzhang.im.common.constant;

/**
 * @author crazycatzhang
 */
public class Constants {

    /**
     * channel绑定的userId Key
     */
    public static final String USERID = "userId";

    /**
     * channel绑定的appId
     */
    public static final String APPID = "appId";

    public static final String CLIENTTYPE = "clientType";

    public static final String IMEI = "imei";

    public static final String READTIME = "readTime";

    public static final String IMCOREZKROOT = "/im-coreRoot";

    public static final String IMCOREZKROOTTCP = "/tcp";

    public static final String IMCOREZKROOTWEBSOCKET = "/webSocket";

    public static class RedisConstants {

        /**
         * userSign，格式：appId:userSign:
         */
        public static final String USERSIGN = "userSign";

        /**
         * 用户上线通知channel
         */
        public static final String USERLOGINCHANNEL
                = "signal/channel/LOGIN_USER_INNER_QUEUE";

        /**
         * 用户session，appId + UserSessionConstants + 用户id 例如10000：userSession：CatZhang
         */
        public static final String USER_SESSION_CONSTANTS = ":userSession:";

        /**
         * 缓存客户端消息防重，格式： appId + :cacheMessage: + messageId
         */
        public static final String CACHEMESSAGE = "cacheMessage";

        public static final String OFFLINEMESSAGE = "offlineMessage";

        /**
         * seq 前缀
         */
        public static final String SEQUENCEPREFIX = "sequence";

        /**
         * 用户订阅列表，格式 ：appId + :subscribe: + userId。Hash结构，filed为订阅自己的人
         */
        public static final String SUBSCRIBE = "subscribe";

        /**
         * 用户自定义在线状态，格式 ：appId + :userCustomerStatus: + userId。set，value为用户id
         */
        public static final String USERCUSTOMERSTATUS = "userCustomerStatus";

    }

    public static class RabbitConstants {

        public static final String IMTOUSERSERVICE = "pipelineToUserService";

        public static final String IMTOMESSAGESERVICE = "pipelineToMessageService";

        public static final String IMTOGROUPSERVICE = "pipelineToGroupService";

        public static final String IMTOFRIENDSHIPSERVICE = "pipelineToFriendshipService";

        public static final String IMTOMEDIASERVICE = "pipelineToMediaService";

        public static final String MESSAGESERVICETOIM = "messageServiceToPipeline";

        public static final String GROUPSERVICETOIM = "GroupServiceToPipeline";

        public static final String FRIENDSHIPTOIM = "friendShipToPipeline";

        public static final String MEDIASERVICETOIM = "MediaServiceToPipeline";

        public static final String STOREPTOPMESSAGE = "storePToPMessage";

        public static final String STOREGROUPMESSAGE = "storeGroupMessage";

    }

    public static class CallbackCommand {
        public static final String MODIFYUSERAFTER = "user.modify.after";

        public static final String CREATEGROUPAFTER = "group.create.after";

        public static final String UPDATEGROUPAFTER = "group.update.after";

        public static final String DESTROYGROUPAFTER = "group.destroy.after";

        public static final String TRANSFERGROUPAFTER = "group.transfer.after";

        public static final String GROUPMEMBERADDBEFORE = "group.member.add.before";

        public static final String GROUPMEMBERADDAFTER = "group.member.add.after";

        public static final String GROUPMEMBERDELETEAFTER = "group.member.delete.after";

        public static final String ADDFRIENDBEFORE = "friend.add.before";

        public static final String ADDFRIENDAFTER = "friend.add.after";

        public static final String UPDATEFRIENDBEFORE = "friend.update.before";

        public static final String UPDATEFRIENDAFTER = "friend.update.after";

        public static final String DELETEFRIENDAFTER = "friend.delete.after";

        public static final String ADDBLACKAFTER = "black.add.after";

        public static final String DELETEBLACK = "black.delete";

        public static final String SENDMESSAGEAFTER = "message.send.after";

        public static final String SENDMESSAGEBEFORE = "message.send.before";

    }

    public static class SequenceConstants {

        public static final String MESSAGE = "messageSequence";

        public static final String GROUPMESSAGE = "groupMessageSequence";


        public static final String FRIENDSHIP = "friendshipSequence";

//        public static final String FriendshipBlack = "friendshipBlackSeq";

        public static final String FRIENDSHIPREQUEST = "friendshipRequestSequence";

        public static final String FRIENDSHIPGROUP = "friendshipGroupSequence";

        public static final String FRIENDSHIPGROUPMEMBER = "friendshipGroupMemberSequence";

        public static final String GROUP = "groupSequence";

        public static final String GROUPREQUEST = "groupRequestSequence";

        public static final String GROUPMEMBER = "groupMemberSequence";

        public static final String CONVERSATION = "conversationSequence";

    }
}
