package com.catzhang.im.tcp.listener;

import com.alibaba.fastjson.JSONObject;
import com.catzhang.im.codec.proto.MessagePack;
import com.catzhang.im.common.ClientType;
import com.catzhang.im.common.constant.Constants;
import com.catzhang.im.common.enums.DeviceMultiLogin;
import com.catzhang.im.common.enums.command.SystemCommand;
import com.catzhang.im.common.model.UserClientDto;
import com.catzhang.im.tcp.redis.RedisManager;
import com.catzhang.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 多端同步：1单端登录：一端在线：踢掉除了本clientType + imel 的设备
 * 2双端登录：允许pc/mobile 其中一端登录 + web端 踢掉除了本clientType + imel 以外的web端设备
 * 3 三端登录：允许手机+pc+web，踢掉同端的其他imei 除了web
 * 4 不做任何处理
 *
 * @author crazycatzhang
 */
@Data
@AllArgsConstructor
public class UserLoginMessageListener {

    private final static Logger logger = LoggerFactory.getLogger(UserLoginMessageListener.class);

    private Integer loginModel;

    public void listenerUserLogin() {
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.USERLOGINCHANNEL);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String message) {
                logger.info("用户已上线:" + message);
                UserClientDto userClientDto = JSONObject.parseObject(message, UserClientDto.class);
                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(userClientDto.getAppId(), userClientDto.getUserId());
                for (NioSocketChannel nioSocketChannel : nioSocketChannels) {
                    if (loginModel == DeviceMultiLogin.ONE.getLoginMode()) {
                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.CLIENTTYPE)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.IMEI)).get();

                        if (!(clientType + ":" + imei).equals(userClientDto.getClientType() + ":" + userClientDto.getImei())) {
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.USERID)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.USERID)).get());
                            pack.setCommand(SystemCommand.MUTUAL_LOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }

                    } else if (loginModel == DeviceMultiLogin.TWO.getLoginMode()) {

                        if (userClientDto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }

                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.CLIENTTYPE)).get();
                        if (clientType == ClientType.WEB.getCode()) {
                            continue;
                        }

                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.IMEI)).get();
                        if (!(clientType + ":" + imei).equals(userClientDto.getClientType() + ":" + userClientDto.getImei())) {
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.USERID)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.USERID)).get());
                            pack.setCommand(SystemCommand.MUTUAL_LOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }

                    } else if (loginModel == DeviceMultiLogin.THREE.getLoginMode()) {

                        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.CLIENTTYPE)).get();
                        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.IMEI)).get();

                        if (userClientDto.getClientType() == ClientType.WEB.getCode()) {
                            continue;
                        }

                        boolean isSameClient = false;
                        if ((clientType == ClientType.IOS.getCode() ||
                                clientType == ClientType.ANDROID.getCode()) &&
                                (userClientDto.getClientType() == ClientType.IOS.getCode() ||
                                        userClientDto.getClientType() == ClientType.ANDROID.getCode())) {
                            isSameClient = true;
                        }

                        if ((clientType == ClientType.MAC.getCode() ||
                                clientType == ClientType.WINDOWS.getCode()) &&
                                (userClientDto.getClientType() == ClientType.MAC.getCode() ||
                                        userClientDto.getClientType() == ClientType.WINDOWS.getCode())) {
                            isSameClient = true;
                        }

                        if (isSameClient && !(clientType + ":" + imei).equals(userClientDto.getClientType() + ":" + userClientDto.getImei())) {
                            MessagePack<Object> pack = new MessagePack<>();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.USERID)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.USERID)).get());
                            pack.setCommand(SystemCommand.MUTUAL_LOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }
                    }
                }
            }
        });
    }
}
