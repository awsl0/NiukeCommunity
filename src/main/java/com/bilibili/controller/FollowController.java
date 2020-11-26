package com.bilibili.controller;

import com.bilibili.event.EventProducer;
import com.bilibili.pojo.Event;
import com.bilibili.pojo.User;
import com.bilibili.service.FollowService;
import com.bilibili.service.UserService;
import com.bilibili.utils.HostUtils;
import com.bilibili.utils.MD5Utils;
import com.bilibili.utils.UserStatus;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements UserStatus {
    @Autowired
    FollowService followService;
    @Autowired
    UserService userService;
    @Autowired
    EventProducer eventProducer;

    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = HostUtils.getUser();
        followService.follow(user.getId(),entityType,entityId);
        //触发关注事件
        Event event = new Event();
        event.setTopic(TOPIC_FOLLOW).setUserId(user.getId())
                .setEntityType(entityType).setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return MD5Utils.getJsonString("200","已关注");
    }
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = HostUtils.getUser();
        followService.unfollow(user.getId(),entityType,entityId);
        return MD5Utils.getJsonString("200","已取关");
    }

    @GetMapping("/followee/{userId}")
    public String followee(@PathVariable("userId") Integer userId, Model model,@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        User user = userService.getUserById(userId);
        if (user==null){
            throw new RuntimeException("没有该用户！");
        }
        model.addAttribute("user",user);
        Page page = PageHelper.startPage(pageNum, 10);
        List<Map<String, Object>> allFollows = followService.followeeList(userId, ENTITY_TYPE_USER, 0, -1);
        page.setTotal(allFollows.size());
        model.addAttribute("page",page);
        List<Map<String, Object>> list = followService.followeeList(userId, ENTITY_TYPE_USER, (int) page.getStartRow(), (int) page.getEndRow()-1);
        for (Map<String, Object> map : list) {
            User u = (User) map.get("user");
            boolean isFollow=HostUtils.getUser()==null?false:followService.isFollow(HostUtils.getUser().getId(),ENTITY_TYPE_USER,u.getId());
            map.put("isFollow", isFollow);
        }
        model.addAttribute("list",list);
        model.addAttribute("path","/followee/"+userId);
        return "site/followee";
    }

    @GetMapping("/follower/{userId}")
    public String follower(@PathVariable("userId") Integer userId, Model model,@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        User user = userService.getUserById(userId);
        if (user==null){
            throw new RuntimeException("没有该用户！");
        }
        model.addAttribute("user",user);
        Page page = PageHelper.startPage(pageNum, 10);
        List<Map<String, Object>> allFollows = followService.followerList(ENTITY_TYPE_USER, userId,0, -1);
        page.setTotal(allFollows.size());
        model.addAttribute("page",page);
        List<Map<String, Object>> list = followService.followerList(ENTITY_TYPE_USER, userId,(int) page.getStartRow(), (int) page.getEndRow()-1);
        for (Map<String, Object> map : list) {
            User u = (User) map.get("user");
            boolean isFollow=HostUtils.getUser()==null?false:followService.isFollow(HostUtils.getUser().getId(),ENTITY_TYPE_USER,u.getId());
            map.put("isFollow", isFollow);
        }
        model.addAttribute("list",list);
        model.addAttribute("path","/follower/"+userId);
        return "site/follower";
    }
}
