package com.bilibili.service.impl;

import com.bilibili.mapper.CommentMapper;
import com.bilibili.pojo.Comment;
import com.bilibili.pojo.DiscussPost;
import com.bilibili.service.CommentService;
import com.bilibili.service.DiscussPostService;
import com.bilibili.utils.MD5Utils;
import com.bilibili.utils.SensitiveFilter;
import com.bilibili.utils.UserStatus;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
@Service
public class CommentServiceImpl implements CommentService, UserStatus {
    @Autowired
    CommentMapper commentMapper;
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    SensitiveFilter sensitiveFilter;

    @Override
    public PageInfo getComments(Integer entityType, Integer entityId,Integer size,Integer pageNum) {
        String orderBy = "create_time asc";
        PageHelper.startPage(pageNum,size,orderBy);
        List<Comment> comments = commentMapper.getComments(entityType, entityId);
        PageInfo<Comment> page= new PageInfo<Comment>(comments);
        return page;
    }


    @Override
    public List<Comment> getComments(Integer entityType, Integer entityId) {
        return commentMapper.getComments(entityType,entityId);
    }

    @Override
    public int CommentCount(Integer entityType, Integer entityId) {
        return commentMapper.CommentCount(entityType,entityId);
    }

    @Override
    public Comment getCommentById(Integer id) {
        return commentMapper.getCommentById(id);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public boolean addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filterText(comment.getContent()));
        boolean b = commentMapper.addComment(comment);
        if (comment.getEntityType()==ENTITY_TYPE_POST){
            int count=commentMapper.CommentCount(ENTITY_TYPE_POST,comment.getEntityId());
            discussPostService.updateDiscussionComment(comment.getEntityId(),count);
        }
        return b;
    }
}
