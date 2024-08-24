package com.bless.common.constant;

/**
 * @Author bless
 * @Version 1.0
 * @Description
 * @Date 2024-08-24 12:39
 */

public class Constants {
    /** channel绑定的userId Key*/
    public static final String UserId = "userId";

    /** channel绑定的appId */
    public static final String AppId = "appId";

    public static final String ClientType = "clientType";

    public static final String ReadTime = "readTime";
    public static class RedisConstants{

        /**
         * 用户session: appId + UserSessionConstants + 用户id
         */
        public static final String UserSessionConstants = ":userSession:";

    }
    public static class RabbitConstants{

        public static final String Im2UserService = "pipeline2UserService";

        public static final String Im2MessageService = "pipeline2MessageService";

        public static final String Im2GroupService = "pipeline2GroupService";

        public static final String Im2FriendshipService = "pipeline2FriendshipService";

        public static final String MessageService2Im = "messageService2Pipeline";

        public static final String GroupService2Im = "GroupService2Pipeline";

        public static final String FriendShip2Im = "friendShip2Pipeline";

        public static final String StoreP2PMessage = "storeP2PMessage";

        public static final String StoreGroupMessage = "storeGroupMessage";


    }

}
