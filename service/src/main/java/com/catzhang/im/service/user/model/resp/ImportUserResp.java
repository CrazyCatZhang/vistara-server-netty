package com.catzhang.im.service.user.model.resp;

import lombok.Data;

import java.util.List;

@Data
public class ImportUserResp {

    private List<String> successIds;

    private List<String> errorIds;
}
