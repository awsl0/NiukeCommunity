package com.bilibili.service;

import java.util.Date;

public interface DataService {
    void recordUV(String ip);

    long calculateUV(Date start, Date end);

    void recordDau(Integer userId);

    long calculateDAU(Date start, Date end);
}
