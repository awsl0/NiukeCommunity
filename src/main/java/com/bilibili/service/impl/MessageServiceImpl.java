package com.bilibili.service.impl;

import com.bilibili.mapper.MessageMapper;
import com.bilibili.pojo.Comment;
import com.bilibili.pojo.Message;
import com.bilibili.service.MessageService;
import com.bilibili.utils.SensitiveFilter;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.Date;
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
public class MessageServiceImpl implements MessageService {
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    SensitiveFilter sensitiveFilter;

    @Override
    public PageInfo getAllConversations(int userId,Integer size,Integer pageNum) {
        String orderBy = "id desc";
        PageHelper.startPage(pageNum,size,orderBy);
        List<Message> conversations = messageMapper.getAllConversations(userId);
        PageInfo<Message> page= new PageInfo<Message>(conversations);
        return page;
    }

    @Override
    public int getAllConversationsCount(int userId) {
        return messageMapper.getAllConversationsCount(userId);
    }

    @Override
    public boolean addMessage(Message message) {
        message.setContent(sensitiveFilter.filterText(message.getContent()));
        message.setStatus(0);
        message.setCreateTime(new Date());
        if (message.getConversationId().isEmpty()) {
            if (message.getFromId() < message.getToId()) {
                message.setConversationId(message.getFromId() + "_" + message.getToId());
            } else {
                message.setConversationId(message.getToId() + "_" + message.getFromId());
            }
        }
        return messageMapper.addMessage(message);
    }

    @Override
    public boolean updateMessageStatus(List<Integer> ids) {
        return messageMapper.updateMessageStatus(ids, 1);
    }

    @Override
    public PageInfo getAllLetters(String conversationId,Integer size,Integer pageNum) {
        String orderBy = "id asc";
        PageHelper.startPage(pageNum,size,orderBy);
        List<Message> letters = messageMapper.getAllLetters(conversationId);
        PageInfo<Message> page= new PageInfo<Message>(letters);
        return page;
    }

    @Override
    public List<Message> getAllLetters(String conversationId) {
        return messageMapper.getAllLetters(conversationId);
    }

    @Override
    public int getAllLettersCount(String conversationId) {
        return messageMapper.getAllLettersCount(conversationId);
    }

    @Override
    public int getAllLettersUnreadCount(Integer userId, String conversationId) {
        return messageMapper.getAllLettersUnreadCount(userId,conversationId);
    }

    @Override
    public Message getLastNotice(Integer userId, String topic) {
        return messageMapper.getLastNotice(userId,topic);
    }

    @Override
    public int getNoticeCount(Integer userId, String topic) {
        return messageMapper.getNoticeCount(userId,topic);
    }

    @Override
    public int getUnreadNoticeCount(Integer userId, String topic) {
        return messageMapper.getUnreadNoticeCount(userId,topic);
    }

    @Override
    public PageInfo getAllNotice(Integer userId, String topic,Integer size,Integer pageNum) {
        String orderBy = "create_time desc";
        PageHelper.startPage(pageNum,size,orderBy);
        List<Message> notices = messageMapper.getAllNotice(userId, topic);
        PageInfo<Message> page= new PageInfo<Message>(notices);
        return page;
    }
}
