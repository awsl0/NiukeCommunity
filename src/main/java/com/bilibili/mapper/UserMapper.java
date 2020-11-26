package com.bilibili.mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */

import com.bilibili.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface UserMapper{
    User getUserById(Integer id);

    boolean addUser(User user);

    User getUserByName(String name);

    User getUserByEmail(String email);

    boolean updateStatus(Integer id,Integer status);

    boolean updateHeader(Integer id,String headerurl);

    boolean updatePassword(Integer id,String password);
}
