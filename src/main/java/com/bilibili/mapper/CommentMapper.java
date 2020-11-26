package com.bilibili.mapper;


import com.bilibili.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
@Mapper
@Repository
public interface CommentMapper{
    //得到所有评论
    List<Comment> getComments(Integer entityType,Integer entityId);
    //得到评论数量
    int CommentCount(Integer entityType,Integer entityId);
    //添加评论
    boolean addComment(Comment comment);
    //根据ID得到评论
    Comment getCommentById(int id);
}
