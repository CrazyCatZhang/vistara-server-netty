package com.catzhang.im.common.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


/**
 * @author crazycatzhang
 */
public class SignApi {
    final private long appId;
    final private String key;

    public SignApi(long appId, String key) {
        this.appId = appId;
        this.key = key;
    }

    public static void main(String[] args) throws InterruptedException {
        SignApi asd = new SignApi(10000, "123456");
        String sign = asd.genUserSign("CatZhang", 100000000);
        JSONObject jsonObject = decodeUserSign(sign);
        System.out.println("sign:" + sign);
        System.out.println("decoder:" + jsonObject.toString());
    }

    /**
     * @param
     * @return com.alibaba.fastjson.JSONObject
     * @description: 解密方法
     * @author lld
     */
    public static JSONObject decodeUserSign(String userSig) {
        JSONObject signDoc = new JSONObject(true);
        try {
            byte[] decodeUrlByte = Base64URL.base64DecodeUrlNotReplace(userSig.getBytes());
            byte[] decompressByte = decompress(decodeUrlByte);
            String decodeText = new String(decompressByte, StandardCharsets.UTF_8);

            if (StringUtils.isNotBlank(decodeText)) {
                signDoc = JSONObject.parseObject(decodeText);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return signDoc;
    }

    /**
     * 解压缩
     *
     * @param data 待压缩的数据
     * @return byte[] 解压缩后的数据
     */
    public static byte[] decompress(byte[] data) {
        byte[] output;

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }


    /**
     * 【功能说明】用于签发 IM 服务中必须要使用的 UserSign 鉴权票据
     * <p>
     * 【参数说明】
     */
    public String genUserSign(String userid, long expire) {
        return genUserSign(userid, expire, null);
    }


    private String hmacsha256(String identifier, long currTime, long expire, String base64Userbuf) {
        String contentToBeSigned = "TLS.identifier:" + identifier + "\n"
                + "TLS.appId:" + appId + "\n"
                + "TLS.expireTime:" + currTime + "\n"
                + "TLS.expire:" + expire + "\n";
        if (null != base64Userbuf) {
            contentToBeSigned += "TLS.userbuf:" + base64Userbuf + "\n";
        }
        try {
            byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HmacSHA256");
            hmac.init(keySpec);
            byte[] byteSig = hmac.doFinal(contentToBeSigned.getBytes(StandardCharsets.UTF_8));
            return (Base64.getEncoder().encodeToString(byteSig)).replaceAll("\\s*", "");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return "";
        }
    }

    private String genUserSign(String userid, long expire, byte[] userbuf) {

        long currTime = System.currentTimeMillis() / 1000;

        JSONObject signDoc = new JSONObject();
        signDoc.put("TLS.identifier", userid);
        signDoc.put("TLS.appId", appId);
        signDoc.put("TLS.expire", expire);
        signDoc.put("TLS.expireTime", currTime);

        String base64UserBuf = null;
        if (null != userbuf) {
            base64UserBuf = Base64.getEncoder().encodeToString(userbuf).replaceAll("\\s*", "");
            signDoc.put("TLS.userBuf", base64UserBuf);
        }
        String sig = hmacsha256(userid, currTime, expire, base64UserBuf);
        if (sig.length() == 0) {
            return "";
        }
        signDoc.put("TLS.sign", sig);
        Deflater compressor = new Deflater();
        compressor.setInput(signDoc.toString().getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedBytesLength = compressor.deflate(compressedBytes);
        compressor.end();
        return (new String(Base64URL.base64EncodeUrl(Arrays.copyOfRange(compressedBytes,
                0, compressedBytesLength)))).replaceAll("\\s*", "");
    }

    public String genUserSign(String userid, long expire, long time, byte[] userbuf) {

        JSONObject signDoc = new JSONObject();
        signDoc.put("TLS.identifier", userid);
        signDoc.put("TLS.appId", appId);
        signDoc.put("TLS.expire", expire);
        signDoc.put("TLS.expireTime", time);

        String base64UserBuf = null;
        if (null != userbuf) {
            base64UserBuf = Base64.getEncoder().encodeToString(userbuf).replaceAll("\\s*", "");
            signDoc.put("TLS.userBuf", base64UserBuf);
        }
        String sig = hmacsha256(userid, time, expire, base64UserBuf);
        if (sig.length() == 0) {
            return "";
        }
        signDoc.put("TLS.sign", sig);
        Deflater compressor = new Deflater();
        compressor.setInput(signDoc.toString().getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedBytesLength = compressor.deflate(compressedBytes);
        compressor.end();
        return (new String(Base64URL.base64EncodeUrl(Arrays.copyOfRange(compressedBytes,
                0, compressedBytesLength)))).replaceAll("\\s*", "");
    }

}