package com.catzhang.im.common.route.algorithm.consistenthash;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author crazycatzhang
 */
public abstract class AbstractConsistentHash {

    protected abstract void add(long key, String value);

    protected void sort() {
    }

    protected abstract String getFirstNodeValue(String value);

    protected abstract void processBefore();

    /**
     * 传入节点列表以及客户端信息获取一个服务节点
     */
    public synchronized String process(List<String> values, String key) {
        processBefore();
        for (String value : values) {
            add(hash(value), value);
        }
        sort();
        return getFirstNodeValue(key);
    }


    /**
     * hash 运算
     */
    public Long hash(String value) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        md5.reset();
        byte[] keyBytes = null;
        keyBytes = value.getBytes(StandardCharsets.UTF_8);

        md5.update(keyBytes);
        byte[] digest = md5.digest();

        // hash code, Truncate to 32-bits
        long hashCode = ((long) (digest[3] & 0xFF) << 24)
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);

        return hashCode & 0xffffffffL;
    }

}
