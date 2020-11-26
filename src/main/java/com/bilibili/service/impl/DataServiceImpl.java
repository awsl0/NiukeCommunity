package com.bilibili.service.impl;

import com.bilibili.service.DataService;
import com.bilibili.utils.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DataServiceImpl implements DataService {
    @Autowired
    RedisTemplate redisTemplate;
    DateFormat df = new SimpleDateFormat("yyyyMMdd");
    Long oneDay = 1000 * 60 * 60 * 24l;

    //将制定的IP放入Uv中
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtils.getUvKey(df.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);
    }
    //统计制定日期的uv
    public long calculateUV(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Long time = start.getTime();
        while (time <= end.getTime()) {
            Date d = new Date(time);
            String key = RedisKeyUtils.getUvKey(df.format(d));
            keyList.add(key);
            time += oneDay;
        }
        // 合并这些数据
        String redisKey = RedisKeyUtils.getUvKey(df.format(start), df.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());
        // 返回统计的结果
        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }
    //将制定的userId放入Uv中
    public void recordDau(Integer userId) {
        String redisKey = RedisKeyUtils.getDauKey(df.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }
    //统计制定日期的dau
    public long calculateDAU(Date start, Date end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 整理该日期范围内的key
        List<String> keyList = new ArrayList<>();
        Long time = start.getTime();
        while (time <= end.getTime()) {
            Date d = new Date(time);
            String key = RedisKeyUtils.getUvKey(df.format(d));
            keyList.add(key);
            time += oneDay;
        }

        // 进行OR运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String redisKey = RedisKeyUtils.getDauKey(df.format(start), df.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return connection.bitCount(redisKey.getBytes());
            }
        });
    }
}
