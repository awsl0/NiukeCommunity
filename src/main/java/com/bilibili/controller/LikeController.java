package com.bilibili.controller;

import com.bilibili.annotation.LoginRequired;
import com.bilibili.event.EventProducer;
import com.bilibili.pojo.Event;
import com.bilibili.pojo.User;
import com.bilibili.service.LikeService;
import com.bilibili.utils.HostUtils;
import com.bilibili.utils.MD5Utils;
import com.bilibili.utils.RedisKeyUtils;
import com.bilibili.utils.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements UserStatus {
    @Autowired
    LikeService likeService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    EventProducer eventProducer;

    @PostMapping("/like")
    @ResponseBody
    @LoginRequired
    public String like(int entityType,int entityId,Integer entityUserId,Integer postId){
        Map map = new HashMap<String, Object>();
        User user = HostUtils.getUser();
        likeService.link(user.getId(),entityType,entityId,entityUserId);
        long likeCount = likeService.LikeCount(entityType, entityId);
        int linkStatus = likeService.LinkStatus(user.getId(), entityType, entityId);
        map.put("likeCount",likeCount);
        map.put("linkStatus",linkStatus);

        //触发点赞事件
        if (linkStatus==1){
            Event event = new Event();
            event.setTopic(TOPIC_LIKE).setUserId(user.getId())
                    .setEntityType(entityType).setEntityId(entityId)
                    .setEntityUserId(entityUserId).setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        if(entityType == ENTITY_TYPE_POST) {
            // 计算帖子分数
            String redisKey = RedisKeyUtils.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }
        return MD5Utils.getJsonString("200",null,map);
    }
}
