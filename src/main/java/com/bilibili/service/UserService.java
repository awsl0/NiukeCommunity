package com.bilibili.service;


import com.bilibili.pojo.LoginTicket;
import com.bilibili.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
public interface UserService{
    User getUserById(Integer id);

    Map<String, Object> register(User user);

    int activation(Integer id,String code);

    Map<String, Object> login(String username, String password, long expires);

    void logout(String ticket);

    LoginTicket findLoginTicket(String ticket);

    boolean updateHeader(Integer id,String headerurl);

    Map<String, Object> updatePassword(User user,String old,String password);

    User getUserByName(String name);

    Collection<? extends GrantedAuthority> getAuthorities(int userId);
}
