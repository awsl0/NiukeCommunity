package com.bilibili.utils;


import com.alibaba.fastjson.JSONObject;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

public class MD5Utils {

    /**
     * MD5加密类
     * @param key 要加密的字符串
     * @return    加密后的字符串
     */
    public static String code(String key){
        if (StringUtils.isEmpty(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    //生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    //json字符串
    public static String getJsonString(String code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code",code);
        json.put("msg",msg);
        if (map!=null){
            for (String s : map.keySet()) {
                json.put(s,map.get(s));
            }
        }
        return json.toJSONString();
    }
    public static String getJsonString(String code, String msg){
        return getJsonString(code,msg,null);
    }
    public static String getJsonString(String code){
        return getJsonString(code,null,null);
    }

}
