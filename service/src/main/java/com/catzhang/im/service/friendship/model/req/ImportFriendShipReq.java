package com.catzhang.im.service.friendship.model.req;

import com.catzhang.im.common.enums.FriendShipStatus;
import com.catzhang.im.common.model.RequestBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author crazycatzhang
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ImportFriendShipReq extends RequestBase {
    @NotBlank(message = "fromId不能为空")
    private String fromId;

    private List<ImportFriendDto> friendItem;

    @Data
    public static class ImportFriendDto {

        private String toId;

        private String remark;

        private String addSource;

        private Integer status = FriendShipStatus.FRIEND_STATUS_NO_FRIEND.getCode();

        private Integer black = FriendShipStatus.BLACK_STATUS_NORMAL.getCode();
    }
}
