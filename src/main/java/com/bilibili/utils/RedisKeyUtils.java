package com.bilibili.utils;

public class RedisKeyUtils {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";

    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getRedisKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }
    // 某个用户收到的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }
    //某个用户关注的实体
    //followee:userId:entityType -> zset(entityId,new)
    public static String getUserFollowee(int userId,int entityType){
        return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }
    //某个实体的粉丝
    //follower:entityType:entityId -> zset(userId,new)
    public static String getEntityFollower(int entityType,int entityId){
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }
    //验证码
    //kaptcha：ownID -> text
    public static String getKaptchaKey(String woner){
        return PREFIX_KAPTCHA+SPLIT+woner;
    }
    //登录凭证
    //ticket:Cooike里面的ticket -> LoginTicket对象
    public static String getTicket(String ticket) {
        return PREFIX_TICKET+SPLIT+ticket;
    }
    //User缓存
    //user:userId -> User对象
    public static String getUserKey(Integer userId){
        return PREFIX_USER+SPLIT+userId;
    }

    //单日uv统计
    //uv:data -> ip地址
    public static String getUvKey(String data){
        return PREFIX_UV+SPLIT+data;
    }
    //区间Uv统计
    public static String getUvKey(String start,String end){
        return PREFIX_UV+SPLIT+start+SPLIT+end;
    }

    //单日dau统计
    //dau:data -> userID
    public static String getDauKey(String data){
        return PREFIX_DAU+SPLIT+data;
    }
    //区间dau统计
    public static String getDauKey(String start,String end){
        return PREFIX_DAU+SPLIT+start+SPLIT+end;
    }

    //统计帖子热度分数
    //post:score
    public static String getPostScoreKey(){
        return PREFIX_POST+SPLIT+"score";
    }
}
