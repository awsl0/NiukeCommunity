package com.bilibili.config.Interceptor;

import com.bilibili.pojo.User;
import com.bilibili.service.DataService;
import com.bilibili.utils.HostUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    DataService dataService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统计uv
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);
        // 统计DAU
        User user = HostUtils.getUser();
        if (user != null) {
            dataService.recordDau(user.getId());
        }
        return true;
    }
}
