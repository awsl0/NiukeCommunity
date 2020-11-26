package com.bilibili.service;

import java.util.List;
import java.util.Map;

public interface FollowService {
    //关注
    void follow(int userId, int entityType, int entityId);
    //取关
    void unfollow(int userId, int entityType, int entityId);
    //查询关注实体类的数量
    long FolloweeCount(int userId,int entityType);
    //查询粉丝数量
    long FollowersCount(int entityType,int entityId);
    //查询是否关注该实体
    boolean isFollow(int userId,int entityType,int entityId);
    //查询某个用户关注列表
    List<Map<String, Object>> followeeList(int userId, int entityType,  Integer start, Integer end);
    //查询某个用户粉丝列表
    List<Map<String, Object>> followerList(int entityType,int entityId, Integer start, Integer end);
}
