package com.catzhang.im.common.enums;

import com.catzhang.im.common.exception.ApplicationExceptionEnum;

/**
 * @author crazycatzhang
 */

public enum FriendShipErrorCode implements ApplicationExceptionEnum {
    IMPORT_SIZE_BEYOND(30000, "导入數量超出上限"),

    ADD_FRIEND_ERROR(30001, "添加好友失败"),

    TO_IS_YOUR_FRIEND(30002, "对方已经是你的好友"),

    TO_IS_NOT_YOUR_FRIEND(30003, "对方不是你的好友"),

    FRIEND_IS_DELETED(30004, "好友已被删除"),

    FRIEND_IS_BLACK(30006, "好友已被拉黑"),

    TARGET_IS_BLACK_YOU(30007, "对方把你拉黑"),

    REPEATSHIP_IS_NOT_EXIST(30008, "关系链记录不存在"),

    ADD_BLACK_ERROR(30009, "添加黑名單失败"),

    FRIEND_IS_NOT_YOUR_BLACK(30010, "好友已經不在你的黑名單内"),

    NOT_APPROVER_OTHER_MAN_REQUEST(30011, "无法审批其他人的好友请求"),

    FRIEND_REQUEST_IS_NOT_EXIST(30012, "好友申请不存在"),

    FRIEND_SHIP_GROUP_CREATE_ERROR(30014, "好友分组创建失败"),

    FRIEND_SHIP_GROUP_IS_EXIST(30015, "好友分组已存在"),

    FRIEND_SHIP_GROUP_IS_NOT_EXIST(30016, "好友分组不存在"),

    YOU_HAVE_NOT_FRIEND_SHIP(30017, "你还没有添加过好友"),

    FRIEND_SHIP_REQUEST_IS_FAILED(30018, "好友申请失败"),

    FRIENDS_ARE_ALREADY_IN_THIS_GROUP(30019, "好友已经在此分组里"),

    FAILED_TO_CLEAR_GROUP_FRIENDS(30020, "分组内好友删除失败"),

    GROUP_DELETION_FAILED(30021, "分组删除失败"),

    ;

    private final int code;
    private final String error;

    FriendShipErrorCode(int code, String error) {
        this.code = code;
        this.error = error;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getError() {
        return this.error;
    }
}
