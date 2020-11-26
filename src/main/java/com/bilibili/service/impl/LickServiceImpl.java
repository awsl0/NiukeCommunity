package com.bilibili.service.impl;

import com.bilibili.pojo.User;
import com.bilibili.service.LikeService;
import com.bilibili.utils.HostUtils;
import com.bilibili.utils.RedisKeyUtils;
import com.bilibili.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LickServiceImpl implements LikeService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public long LikeCount(int entityType, int entityId) {
        String key = RedisKeyUtils.getRedisKey(entityType, entityId);
        return redisUtil.sGetSetSize(key);
    }

    @Override
    public int LinkStatus(int userId, int entityType, int entityId) {
        String key = RedisKeyUtils.getRedisKey(entityType, entityId);
        return redisUtil.sHasKey(key, userId) ? 1 : 0;
    }

    @Override
    public void link(int userId, int entityType, int entityId,int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtils.getRedisKey(entityType, entityId);
                String userLikeKey = RedisKeyUtils.getUserLikeKey(entityUserId);
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                operations.multi();
                if (isMember) {
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    public int userLikeCount(int userId){
        String userLikeKey = RedisKeyUtils.getUserLikeKey(userId);
        return redisUtil.get(userLikeKey)==null? 0 : (int) redisUtil.get(userLikeKey);
    }
}
