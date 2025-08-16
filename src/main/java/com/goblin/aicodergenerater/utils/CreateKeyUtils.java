package com.goblin.aicodergenerater.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.util.DigestUtils;

/**
 * 缓存 key 生成工具类
 * @Author goblin
 * @Date 2025/8/15 21:51
 * @注释
 */
public class CreateKeyUtils {
    public static String generateKey(Object obj){
        if(obj == null){
            return DigestUtil.md5Hex("null");
        }
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);
    }
}
