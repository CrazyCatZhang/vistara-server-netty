package com.catzhang.im.common.enums;


import com.catzhang.im.common.exception.ApplicationExceptionEnum;

/**
 * @author crazycatzhang
 */

public enum GroupErrorCode implements ApplicationExceptionEnum {

    GROUP_IS_NOT_EXIST(40000, "群组不存在"),

    GROUP_IS_EXIST(40001, "群组已存在"),

    GROUP_IS_HAVE_OWNER(40002, "群已存在群主"),

    USER_IS_JOINED_GROUP(40003, "该用户已经进入该群"),

    USER_JOIN_GROUP_ERROR(40004, "群成员添加失败"),

    GROUP_MEMBER_IS_BEYOND(40005, "群成员已达到上限"),

    MEMBER_IS_NOT_JOINED_GROUP(40006, "该用户不在群内"),

    THIS_OPERATE_NEED_MANAGER_ROLE(40007, "该操作只允许群主/管理员操作"),

    THIS_OPERATE_NEED_APPMANAGER_ROLE(40008, "该操作只允许APP管理员操作"),

    THIS_OPERATE_NEED_OWNER_ROLE(40009, "该操作只允许群主操作"),

    GROUP_OWNER_IS_NOT_REMOVE(40010, "群主无法移除"),

    UPDATE_GROUP_BASE_INFO_ERROR(40011, "更新群信息失败"),

    THIS_GROUP_IS_MUTE(40012, "该群禁止发言"),

    IMPORT_GROUP_ERROR(40013, "导入群组失败"),

    THIS_OPERATE_NEED_ONESELF(40014, "该操作只允许自己操作"),

    PRIVATE_GROUP_CAN_NOT_DESTORY(40015, "私有群不允许解散"),

    PUBLIC_GROUP_MUST_HAVE_OWNER(40016, "公开群必须指定群主"),

    GROUP_MEMBER_IS_SPEAK(40017, "群成员被禁言"),

    GROUP_IS_DESTROY(40018, "群组已解散"),

    CREATE_GROUP_IS_FAILED(40019, "创建群组失败"),

    FAILED_TO_REMOVE_GROUP_MEMBERS(40020, "移除群成员失败"),

    GROUP_OWNER_CAN_ONLY_TRANSFER(40021, "群主只能转让"),

    MUTING_FAILED(40022, "禁言失败"),

    NOT_TRANSFERABLE_TO_ONESELF(40023, "不能转让给自己"),

    PRIVATE_GROUPS_ARE_NOT_ALLOWED_TO_MUTE(40024, "私有群不允许禁言"),

    GROUP_REQUEST_IS_FAILED(40025, "加群申请失败"),

    PRIVATE_GROUPS_ARE_INVITE_ONLY(40026, "私有群只能邀请加入"),

    NO_ONE_IS_ALLOWED_IN_THIS_GROUP(40027, "该群禁止任何人加入"),

    ADD_GROUP_APPLICATION_IS_NOT_EXIST(40028, "加群申请不存在"),

    GROUP_REQUEST_APPROVED(40029, "加群申请已审批");

    private final int code;
    private final String error;

    GroupErrorCode(int code, String error) {
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
