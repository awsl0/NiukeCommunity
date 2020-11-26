package com.bilibili.service;

public interface LikeService {
    //点赞数量
    long LikeCount(int entityType,int entityId);
    //点赞状态
    int LinkStatus(int userId,int entityType,int entityId);
    //点赞
    void link(int userId,int entityType,int entityId,int entityUserId);
    //得到某个用户得到赞的数量
    int userLikeCount(int userId);
}
