package com.catzhang.im.service.group.model.req;

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
public class GetJoinedGroupReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String memberId;

    //群类型
    private List<Integer> groupType;

    //单次拉取的群组数量，如果不填代表所有群组
    private Integer limit;

    //第几页
    private Integer offset;

}