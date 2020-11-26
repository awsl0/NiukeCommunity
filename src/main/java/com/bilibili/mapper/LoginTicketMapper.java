package com.bilibili.mapper;

import com.bilibili.pojo.LoginTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * <p>
 *  Mapper 接口
 *  为提高并发处理速度，将loginTicket存入Redis中处理
 *  故改Mapper不在使用
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
@Mapper
@Repository
@Deprecated
public interface LoginTicketMapper {
    //添加一个LoginTicket
    boolean addLoginTicket(LoginTicket loginTicket);
    //根据凭证查询LoginTicket
    LoginTicket getLoginTicketByTicket(String ticket);
    //修改凭证状态
    boolean updateStatus(@Param("ticket")String ticket,@Param("status")Integer status);
}
