package com.bilibili.service.impl;


import com.bilibili.mapper.DiscussPostMapper;
import com.bilibili.pojo.DiscussPost;
import com.bilibili.service.DiscussPostService;
import com.bilibili.service.UserService;
import com.bilibili.utils.HostUtils;
import com.bilibili.utils.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
@Service
@Slf4j
public class DiscussPostServiceImpl implements DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    SensitiveFilter sensitiveFilter;
    @Value("${caffeine.posts.max-size}")
    private int maxSize;
    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // 帖子列表缓存
    private LoadingCache<String, PageInfo> postListCache;

    @PostConstruct
    public void init(){
        //初始化帖子列表
        postListCache= Caffeine.newBuilder()
                .maximumSize(maxSize).expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, PageInfo>() {
                    @Nullable
                    @Override
                    public PageInfo load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        String[] params = key.split(":");
                        if (params.length!=3||params==null){
                            throw new IllegalArgumentException("参数错误!");
                        }
                        log.info("在缓存中读取热度排行");
                        PageHelper.startPage(Integer.valueOf(params[1]),Integer.valueOf(params[2]),params[0]);
                        List<DiscussPost> list = discussPostMapper.findAllDiscussPost(0);
                        PageInfo<DiscussPost> page= new PageInfo<DiscussPost>(list);
                        return page;
                    }
                });
    }

    @Override
    public boolean updateScore(Integer id, Double score) {
        return discussPostMapper.updateScore(id,score);
    }

    @Override
    public PageInfo findAllDiscussPost(Integer userId,Integer pageNum,int size,int orderMode) {
        String orderBy="";
        if (userId==0 && orderMode==1){
            orderBy = "type desc, score desc, create_time desc";
            return postListCache.get(orderBy+":"+pageNum+":"+size);
        }
        else {
            log.info("load post list from DB.");
            orderBy = "type desc,create_time desc";
        }
        PageHelper.startPage(pageNum,size,orderBy);
        List<DiscussPost> list = discussPostMapper.findAllDiscussPost(userId);
        PageInfo<DiscussPost> page= new PageInfo<DiscussPost>(list);
        return page;
    }

    @Override
    public boolean addDiscussion(DiscussPost post) {
        if (post==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filterText(post.getTitle()));
        post.setContent(sensitiveFilter.filterText(post.getContent()));
        post.setType(post.getType()==null?0:post.getType());
        post.setStatus(post.getStatus()==null?0:post.getStatus());
        post.setCommentCount(post.getCommentCount()==null?0:post.getCommentCount());
        post.setScore(post.getScore()==null?0:post.getScore());
        return discussPostMapper.addDiscussion(post);
    }

    @Override
    public DiscussPost getDiscussPostById(Integer id) {
        DiscussPost discussPost=discussPostMapper.getDiscussPostById(id);
        return discussPost;
    }

    @Override
    public boolean updateDiscussionComment(Integer id, int count) {
        return discussPostMapper.updateDiscussionComment(id,count);
    }

    @Override
    public boolean updateType(Integer id, int type) {
        return discussPostMapper.updateType(id,type);
    }

    @Override
    public boolean updateStatus(Integer id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }
}
