package com.bilibili.controller;


import com.bilibili.annotation.LoginRequired;
import com.bilibili.event.EventProducer;
import com.bilibili.pojo.Comment;
import com.bilibili.pojo.DiscussPost;
import com.bilibili.pojo.Event;
import com.bilibili.service.CommentService;
import com.bilibili.service.DiscussPostService;
import com.bilibili.utils.HostUtils;
import com.bilibili.utils.RedisKeyUtils;
import com.bilibili.utils.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements UserStatus {
    @Autowired
    CommentService commentService;
    @Autowired
    EventProducer eventProducer;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    DiscussPostService discussPostService;

    @LoginRequired
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") String discussPostId, Comment comment){
        comment.setCreateTime(new Date());
        comment.setUserId(HostUtils.getUser().getId());
        comment.setStatus(0);
        commentService.addComment(comment);
        //触发评论事件
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT).setUserId(comment.getUserId()).setData("postId",discussPostId)
                .setEntityType(comment.getEntityType()).setEntityId(comment.getEntityId());
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target =discussPostService.getDiscussPostById(comment.getEntityId());
            event.setEntityUserId(Integer.valueOf(target.getUserId()));
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.getCommentById(comment.getTargetId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            // 触发发帖事件
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(Integer.valueOf(discussPostId));
            eventProducer.fireEvent(event);
            // 计算帖子分数
            String redisKey = RedisKeyUtils.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        return "redirect:/discuss-post/detail/"+discussPostId;
    }
}
