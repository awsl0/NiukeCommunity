package com.bilibili.utils;

import com.bilibili.pojo.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息,用于代替session对象.
 */
@Component
public class HostUtils{
    private static ThreadLocal<User> local= new ThreadLocal<>();

    public static void setUser(User user){
        local.set(user);
    }

    public static User getUser(){
        return local.get();
    }

    public static void clear(){
        local.remove();
    }
}
