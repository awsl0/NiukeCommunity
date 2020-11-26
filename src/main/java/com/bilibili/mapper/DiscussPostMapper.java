package com.bilibili.mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */

import com.bilibili.pojo.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DiscussPostMapper{
    //查找所有帖子
    List<DiscussPost> findAllDiscussPost(Integer userId);
    //添加帖子
    boolean addDiscussion(DiscussPost discussPost);
    //查询帖子
    DiscussPost getDiscussPostById(Integer id);
    //修改帖子评论数量
    boolean updateDiscussionComment(Integer id,int count);
    //更改帖子置顶状态
    boolean updateType(Integer id,int type);
    //更改帖子精华状态
    boolean updateStatus(Integer id,int status);
    //删除帖子
    boolean deleteDiscussPost(Integer id);
    //更新帖子热度分数
    boolean updateScore(Integer id,Double score);
}
