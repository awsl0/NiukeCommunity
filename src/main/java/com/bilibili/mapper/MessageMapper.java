package com.bilibili.mapper;

import com.bilibili.pojo.Message;
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
public interface MessageMapper{
    //查询所有的会话
    List<Message> getAllConversations(int userId);
    //查询所有会话的数量
    int getAllConversationsCount(int userId);
    //查询某个会话的所有消息
    List<Message> getAllLetters(String conversationId);
    //查询某个会话的所有消息的数量
    int getAllLettersCount(String conversationId);
    //查询某个会话未读消息的数量
    int getAllLettersUnreadCount(int userId,String conversationId);
    //添加消息
    boolean addMessage(Message message);
    //更改消息已读状态
    boolean updateMessageStatus(List<Integer> ids,Integer status);
    //查询某个主题下最新通知
    Message getLastNotice(Integer userId,String topic);
    //查询某个主题下通知数量
    int getNoticeCount(Integer userId,String topic);
    //查询某个主题下未读消息的数量
    int getUnreadNoticeCount(Integer userId,String topic);
    //查询某个主题下全部通知
    List<Message> getAllNotice(Integer userId,String topic);
}
