package com.bilibili.controller;


import com.alibaba.fastjson.JSONObject;
import com.bilibili.annotation.LoginRequired;
import com.bilibili.pojo.Message;
import com.bilibili.pojo.User;
import com.bilibili.service.MessageService;
import com.bilibili.service.UserService;
import com.bilibili.utils.HostUtils;
import com.bilibili.utils.MD5Utils;
import com.bilibili.utils.UserStatus;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
@Controller
@RequestMapping("/message")
public class MessageController implements UserStatus {
    @Autowired
    MessageService messageService;
    @Autowired
    UserService userService;

    @GetMapping("/list")
    @LoginRequired
    public String getConversationsList(@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum, Model model){
        User user = HostUtils.getUser();
        PageInfo pageInfo = messageService.getAllConversations(user.getId(), 5, pageNum);
        model.addAttribute("page",pageInfo);
        List<Message> conversationList = pageInfo.getList();
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList!=null){
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.getAllLettersCount(message.getConversationId()));
                map.put("unreadCount", messageService.getAllLettersUnreadCount(user.getId(), message.getConversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.getUserById(targetId));
                conversations.add(map);
            }
            model.addAttribute("conversations", conversations);
        }
        model.addAttribute("path","/message/list");
        int letterUnreadCount = messageService.getAllLettersUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.getUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "site/letter";
    }

    @GetMapping("/detail/{conversationId}")
    @LoginRequired
    public String detail(@PathVariable String conversationId,Model model,@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        PageInfo pageInfo = messageService.getAllLetters(conversationId, 5, pageNum);
        model.addAttribute("page",pageInfo);
        List<Message> letterList= pageInfo.getList();
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                map.put("fromUser", userService.getUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));
        //将该会话内所有消息设为已读
        this.updateMessageStatus(conversationId);
        return "/site/letter-detail";
    }

    @PostMapping("/send")
    @ResponseBody
    @LoginRequired
    public String addMessage(String toName,String content){
        Message message = new Message();
        message.setFromId(HostUtils.getUser().getId());
        message.setToId(userService.getUserByName(toName).getId());
        message.setContent(content);
        messageService.addMessage(message);
        return MD5Utils.getJsonString("200");
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (HostUtils.getUser().getId() == id0) {
            return userService.getUserById(id1);
        } else {
            return userService.getUserById(id0);
        }
    }

    private void updateMessageStatus(String conversationId){
        List<Integer> ids = new ArrayList<>();
        List<Message> letters = messageService.getAllLetters(conversationId);
        if (!letters.isEmpty()){
            for (Message letter : letters) {
                if (HostUtils.getUser().getId().equals(letter.getToId()) && letter.getStatus() == 0) {
                    ids.add(letter.getId());
                }
            }
        }
        if (!ids.isEmpty()){
            messageService.updateMessageStatus(ids);
        }
    }

    @GetMapping("/notice")
    @LoginRequired
    public String toNoticePage(Model model){
        User user = HostUtils.getUser();
        // 查询评论类通知
        Message notice = messageService.getLastNotice(user.getId(), TOPIC_COMMENT);
        Map messageVO=new HashMap<String, Object>();
        if (notice!=null){
            messageVO.put("notice",notice);
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String, Object> map= JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user",userService.getUserById((Integer) map.get("userId")));
            messageVO.put("entityType",map.get("entityType"));
            messageVO.put("entityId",map.get("entityId"));
            messageVO.put("postId",map.get("postId"));
            int count=messageService.getNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVO.put("count",count);
            int unreadCount=messageService.getUnreadNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVO.put("unreadCount",unreadCount);
        }
        model.addAttribute("comment",messageVO);
        // 查询点赞类通知
        notice = messageService.getLastNotice(user.getId(), TOPIC_LIKE);
        messageVO=new HashMap<String, Object>();
        if (notice!=null){
            messageVO.put("notice",notice);
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String, Object> map= JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user",userService.getUserById((Integer) map.get("userId")));
            messageVO.put("entityType",map.get("entityType"));
            messageVO.put("entityId",map.get("entityId"));
            messageVO.put("postId",map.get("postId"));
            int count=messageService.getNoticeCount(user.getId(),TOPIC_LIKE);
            messageVO.put("count",count);
            int unreadCount=messageService.getUnreadNoticeCount(user.getId(),TOPIC_LIKE);
            messageVO.put("unreadCount",unreadCount);
        }
        model.addAttribute("like",messageVO);
        // 查询关注类通知
        notice = messageService.getLastNotice(user.getId(), TOPIC_FOLLOW);
        messageVO=new HashMap<String, Object>();
        if (notice!=null){
            messageVO.put("notice",notice);
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map<String, Object> map= JSONObject.parseObject(content, HashMap.class);
            messageVO.put("user",userService.getUserById((Integer) map.get("userId")));
            messageVO.put("entityType",map.get("entityType"));
            messageVO.put("entityId",map.get("entityId"));
            int count=messageService.getNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVO.put("count",count);
            int unreadCount=messageService.getUnreadNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVO.put("unreadCount",unreadCount);
        }
        model.addAttribute("follow",messageVO);
        // 查询未读消息数量
        int letterUnreadCount = messageService.getAllLettersUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.getUnreadNoticeCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/notice";
    }

    @GetMapping("/notice/detail/{topic}")
    @LoginRequired
    public String noticeDetail(@PathVariable("topic") String topic,Model model,@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        User user = HostUtils.getUser();
        PageInfo pageInfo = messageService.getAllNotice(user.getId(), topic, 10, pageNum);
        model.addAttribute("page",pageInfo);
        model.addAttribute("path","/message/notice/detail/"+topic);
        List<Message> list = pageInfo.getList();
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        List ids=new ArrayList<Integer>();
        for (Message message : list) {
            Map<String, Object> map=new HashMap();
            map.put("notice",message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data= JSONObject.parseObject(content, HashMap.class);
            map.put("user",userService.getUserById((Integer) data.get("userId")));
            map.put("entityType",data.get("entityType"));
            map.put("entityId",data.get("entityId"));
            map.put("postId",data.get("postId"));
            noticeVoList.add(map);
            ids.add(message.getId());
        }
        model.addAttribute("noticeVoList",noticeVoList);
        model.addAttribute("system",userService.getUserById(SYSTEM_USER_ID));
        //设置已读
        if (!ids.isEmpty())
            messageService.updateMessageStatus(ids);
        return "site/notice-detail";
    }
}
