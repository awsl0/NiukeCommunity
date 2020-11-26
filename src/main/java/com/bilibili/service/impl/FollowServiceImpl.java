package com.bilibili.service.impl;

import com.bilibili.pojo.DiscussPost;
import com.bilibili.pojo.User;
import com.bilibili.service.FollowService;
import com.bilibili.service.UserService;
import com.bilibili.utils.RedisKeyUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowServiceImpl implements FollowService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    //关注
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtils.getUserFollowee(userId,entityType);
                String followerKey = RedisKeyUtils.getEntityFollower(entityType,entityId);

                operations.multi();

                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return operations.exec();
            }
        });
    }
    //取关
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtils.getUserFollowee(userId,entityType);
                String followerKey = RedisKeyUtils.getEntityFollower(entityType,entityId);

                operations.multi();

                operations.opsForZSet().remove(followeeKey, entityId);
                operations.opsForZSet().remove(followerKey, userId);

                return operations.exec();
            }
        });
    }
    //查询关注实体类的数量
    public long FolloweeCount(int userId,int entityType){
        String followeeKey = RedisKeyUtils.getUserFollowee(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }
    //查询粉丝数量
    public long FollowersCount(int entityType,int entityId){
        String followerKey = RedisKeyUtils.getEntityFollower(entityType,entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }
    //查询是否关注该实体
    public boolean isFollow(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtils.getUserFollowee(userId,entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId)!=null;
    }

    @Override
    public List<Map<String, Object>> followeeList(int userId, int entityType, Integer start, Integer end) {
        String followeeKey = RedisKeyUtils.getUserFollowee(userId,entityType);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, start, end);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map map=new HashMap<String, Object>();
            User user = userService.getUserById(targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> followerList(int entityType, int entityId, Integer start, Integer end) {
        String followerKey = RedisKeyUtils.getEntityFollower(entityType,entityId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, start,end);
        if (targetIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map map=new HashMap<String, Object>();
            User user = userService.getUserById(targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

}
