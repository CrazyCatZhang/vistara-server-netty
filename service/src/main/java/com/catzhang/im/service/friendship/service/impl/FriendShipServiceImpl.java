package com.catzhang.im.service.friendship.service.impl;

import com.catzhang.im.common.ResponseVO;
import com.catzhang.im.common.enums.FriendShipErrorCode;
import com.catzhang.im.service.friendship.dao.FriendShipEntity;
import com.catzhang.im.service.friendship.dao.mapper.FriendShipMapper;
import com.catzhang.im.service.friendship.model.req.ImportFriendShipReq;
import com.catzhang.im.service.friendship.model.resp.ImportFriendShipResp;
import com.catzhang.im.service.friendship.service.FriendShipService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author crazycatzhang
 */
@Service
public class FriendShipServiceImpl implements FriendShipService {

    @Autowired
    FriendShipMapper friendShipMapper;

    @Override
    public ResponseVO<ImportFriendShipResp> importFriendShip(ImportFriendShipReq req) {
        if (req.getFriendItem().size() > 100) {
            return ResponseVO.errorResponse(FriendShipErrorCode.IMPORT_SIZE_BEYOND);
        }
        ImportFriendShipResp importFriendShipResp = new ImportFriendShipResp();
        List<String> successIds = new ArrayList<>();
        List<String> errorIds = new ArrayList<>();
        req.getFriendItem().forEach(item -> {
            FriendShipEntity friendShipEntity = new FriendShipEntity();
            BeanUtils.copyProperties(item, friendShipEntity);
            friendShipEntity.setAppId(req.getAppId());
            friendShipEntity.setFromId(req.getFromId());
            try {
                int insert = friendShipMapper.insert(friendShipEntity);
                if (insert == 1) {
                    successIds.add(item.getToId());
                } else {
                    errorIds.add(item.getToId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorIds.add(item.getToId());
            }
        });

        importFriendShipResp.setSuccessIds(successIds);
        importFriendShipResp.setErrorIds(errorIds);
        return ResponseVO.successResponse(importFriendShipResp);
    }
}
