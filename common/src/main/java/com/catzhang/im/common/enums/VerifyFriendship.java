package com.catzhang.im.common.enums;

/**
 * @author crazycatzhang
 */

public enum VerifyFriendship {
    UNIDIRECTIONAL_VERIFICATION_RESULT_IS_FRIEND(1, "你已添加对方"),
    UNIDIRECTIONAL_VERIFICATION_RESULT_IS_NO_RELATIONSHIP(0, "你未添加对方"),
    BIDIRECTIONAL_VERIFICATION_RESULT_IS_FRIEND(1, "你与对方已是好友关系"),
    BIDIRECTIONAL_VERIFICATION_RESULT_IS_A_ADDS_B(2, "你已添加对方，对方尚未添加你为好友"),
    BIDIRECTIONAL_VERIFICATION_RESULT_IS_B_ADDS_A(3, "对方已添加你，你尚未添加对方为好友"),
    BIDIRECTIONAL_VERIFICATION_RESULT_IS_NO_RELATIONSHIP(4, "你与对方不是好友关系"),
    UNIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_NORMAL(1, "你未拉黑对方"),
    UNIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_BLACKED(0, "你已拉黑对方"),
    BIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_NORMAL(1, "你与对方都处于未拉黑状态"),
    BIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_B_BLACKED_A(2, "你未拉黑对方，对方拉黑了你"),
    BIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_A_BLACKED_B(3, "你拉黑了对方，对方未拉黑你"),
    BIDIRECTIONAL_VERIFICATION_BLACK_RESULT_IS_BLACKED(4, "你与对方都互相拉黑了"),
    THE_OTHER_USER_DOES_NOT_EXIST(-1, "对方用户不存在");
    private final int status;
    private final String message;

    VerifyFriendship(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
