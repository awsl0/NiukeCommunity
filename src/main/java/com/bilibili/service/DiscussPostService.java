package com.bilibili.service;


import com.bilibili.pojo.DiscussPost;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
public interface DiscussPostService{
    PageInfo findAllDiscussPost(Integer userId,Integer pageNum,int size,int orderMode);

    boolean addDiscussion(DiscussPost discussPost);

    DiscussPost getDiscussPostById(Integer id);

    boolean updateDiscussionComment(Integer id,int count);

    boolean updateType(Integer id , int type);

    boolean updateStatus(Integer id , int status);

    boolean updateScore(Integer id,Double score);
}
