package com.bilibili.controller;


import com.bilibili.event.EventProducer;
import com.bilibili.pojo.Comment;
import com.bilibili.pojo.DiscussPost;
import com.bilibili.pojo.Event;
import com.bilibili.pojo.User;
import com.bilibili.service.CommentService;
import com.bilibili.service.DiscussPostService;
import com.bilibili.service.LikeService;
import com.bilibili.service.UserService;
import com.bilibili.utils.HostUtils;
import com.bilibili.utils.MD5Utils;
import com.bilibili.utils.RedisKeyUtils;
import com.bilibili.utils.UserStatus;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
@Controller
@RequestMapping("/discuss-post")
public class DiscussPostController implements UserStatus {
    @Autowired
    DiscussPostService discussPostService;
    @Autowired
    CommentService commentService;
    @Autowired
    UserService userService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    LikeService likeService;
    @Autowired
    EventProducer eventProducer;

    @PostMapping("/add")
    @ResponseBody
    public String addPost(String title,String content){
        User user = HostUtils.getUser();
        if (user==null){
            return MD5Utils.getJsonString("403","你还没有登录哦!");
        }
        DiscussPost post = new DiscussPost();
        post.setCreateTime(new Date());
        post.setUserId(String.valueOf(user.getId()));
        post.setContent(content);
        post.setTitle(title);
        discussPostService.addDiscussion(post);
        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);
        // 计算帖子分数
        String redisKey = RedisKeyUtils.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());
        return MD5Utils.getJsonString("200","发布成功");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussion(@PathVariable("discussPostId") String discussPostId, Model model,@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        if (StringUtils.isEmpty(discussPostId)){
            throw new IllegalArgumentException("参数不能为空");
        }
        Map likeMap=new HashMap<String, Object>();
        DiscussPost post = discussPostService.getDiscussPostById(Integer.valueOf(discussPostId));
        model.addAttribute("post",post);
        User postUser = userService.getUserById(Integer.valueOf(post.getUserId()));
        model.addAttribute("postUser",postUser);
        long likeCount = likeService.LikeCount(ENTITY_TYPE_POST, post.getId());
        likeMap.put("likeCount",likeCount);
        int likeStatus=HostUtils.getUser()==null?0:likeService.LinkStatus(HostUtils.getUser().getId(),ENTITY_TYPE_POST,post.getId());
        likeMap.put("likeStatus",likeStatus);
        model.addAttribute("like",likeMap);
        PageInfo pageInfo = commentService.getComments(ENTITY_TYPE_POST, post.getId(), 5, pageNum);
        model.addAttribute("page",pageInfo);
        List<Comment> comments = pageInfo.getList();
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (comments!=null){
            for (Comment comment : comments) {
                Map<String, Object> commentVo = new HashMap<>();
                User user=userService.getUserById(comment.getUserId());
                commentVo.put("user",user);
                commentVo.put("comment",comment);
                long commentLikeCount = likeService.LikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                int commentLikeStatus=HostUtils.getUser()==null?0:likeService.LinkStatus(HostUtils.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("commentLikeCount",commentLikeCount);
                commentVo.put("commentLikeStatus",commentLikeStatus);
                List<Comment> replyList=commentService.getComments(ENTITY_TYPE_COMMENT,comment.getId());
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList!=null){
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        User replyUser=userService.getUserById(reply.getUserId());
                        replyVo.put("user",replyUser);
                        replyVo.put("reply",reply);
                        long replyLikeCount = likeService.LikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        int replyLikeStatus=HostUtils.getUser()==null?0:likeService.LinkStatus(HostUtils.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                        replyVo.put("replyLikeCount",replyLikeCount);
                        replyVo.put("replyLikeStatus",replyLikeStatus);
                        User target = reply.getTargetId() == 0 ? null : userService.getUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replyList",replyVoList);
                int replyCount = commentService.CommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        model.addAttribute("path","/discuss-post/detail/"+discussPostId);
        return "site/discuss-detail";
    }

    @PostMapping("/top")
    @ResponseBody
    public String setTop(Integer discussPostId){
        discussPostService.updateType(discussPostId,1);
        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(HostUtils.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
        eventProducer.fireEvent(event);
        return MD5Utils.getJsonString("200");
    }

    @PostMapping("/wonderful")
    @ResponseBody
    public String wonderful(Integer discussPostId){
        discussPostService.updateStatus(discussPostId,1);
        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(HostUtils.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
        eventProducer.fireEvent(event);
        // 计算帖子分数
        String redisKey = RedisKeyUtils.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPostId);
        return MD5Utils.getJsonString("200");
    }

    @PostMapping("/delete")
    @ResponseBody
    public String delete(Integer discussPostId){
        discussPostService.updateStatus(discussPostId,2);
        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(HostUtils.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(discussPostId);
        eventProducer.fireEvent(event);
        return MD5Utils.getJsonString("200");
    }
}
