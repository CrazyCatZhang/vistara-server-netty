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

    public static final String READTIME = "readTime";

    public static class RedisConstants {

        public static final String USER_SESSION_CONSTANTS = ":userSession:";

    }

    public static class RabbitConstants {

        public static final String IMTOUSERSERVICE = "pipelineToUserService";

        public static final String IMTOMESSAGESERVICE = "pipelineToMessageService";

        public static final String IMTOGROUPSERVICE = "pipelineToGroupService";

        public static final String IMTOFRIENDSHIPSERVICE = "pipelineToFriendshipService";

        public static final String MESSAGESERVICETOIM = "messageServiceToPipeline";

        public static final String GROUPSERVICETOIM = "GroupServiceToPipeline";

        public static final String FRIENDSHIPTOIM = "friendShipToPipeline";

        public static final String STOREPTOPMESSAGE = "storePToPMessage";

        public static final String STOREGROUPMESSAGE = "storeGroupMessage";

    }

}
