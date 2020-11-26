package com.bilibili.service;


import com.bilibili.pojo.Comment;
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
public interface CommentService{
    PageInfo getComments(Integer entityType, Integer entityId,Integer site,Integer pageNum);

    List<Comment> getComments(Integer entityType, Integer entityId);

    int CommentCount(Integer entityType,Integer entityId);

    boolean addComment(Comment comment);

    Comment getCommentById(Integer id);
}
