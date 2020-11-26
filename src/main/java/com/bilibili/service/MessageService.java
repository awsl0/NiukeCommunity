package com.bilibili.service;

import com.bilibili.pojo.Message;
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
public interface MessageService{
    //查询所有的会话
    PageInfo getAllConversations(int userId, Integer size, Integer pageNum);
    //查询所有会话的数量
    int getAllConversationsCount(int userId);
    //查询某个会话的所有消息
    PageInfo getAllLetters(String conversationId,Integer size,Integer pageNum);

    List<Message> getAllLetters(String conversationId);
    //查询某个会话的所有消息的数量
    int getAllLettersCount(String conversationId);
    //查询某个会话未读消息的数量
    int getAllLettersUnreadCount(Integer userId,String conversationId);
    //添加消息
    boolean addMessage(Message message);
    //更改消息已读状态
    boolean updateMessageStatus(List<Integer> ids);
    //查询某个主题下最新通知
    Message getLastNotice(Integer userId,String topic);
    //查询某个主题下通知数量
    int getNoticeCount(Integer userId,String topic);
    //查询某个主题下未读消息的数量
    int getUnreadNoticeCount(Integer userId,String topic);
    //查询某个主题下全部通知
    PageInfo getAllNotice(Integer userId, String topic,Integer size,Integer pageNum);
}
