package com.bilibili.service.impl;

import com.bilibili.mapper.LoginTicketMapper;
import com.bilibili.mapper.UserMapper;
import com.bilibili.pojo.LoginTicket;
import com.bilibili.pojo.User;
import com.bilibili.service.UserService;
import com.bilibili.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
@Service
public class UserServiceImpl implements UserService, UserStatus {
    @Autowired
    UserMapper userMapper;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    MailUtil mailUtil;
    @Autowired
    RedisUtil redisUtil;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public User getUserById(Integer id) {
        User user = getRedisUser(id);
        if (user==null){
            user = initRedisUser(id);
        }
        return user;
    }

    @Override
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        if (user==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isEmpty(user.getUsername())){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if (StringUtils.isEmpty(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }
        if (StringUtils.isEmpty(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if (user.getPassword().length()<8){
            map.put("passwordMsg","密码长度不能小与8位！");
            return map;
        }
        if (userMapper.getUserByName(user.getUsername())!=null){
            map.put("usernameMsg","账号已存在");
            return map;
        }
        if (userMapper.getUserByEmail(user.getEmail())!=null) {
            map.put("emailMsg","邮箱已被注册！");
            return map;
        }
        user.setSalt(MD5Utils.generateUUID().substring(0,5));
        user.setCreateTime(new Date());
        user.setPassword(MD5Utils.code(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(MD5Utils.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        userMapper.addUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content =templateEngine.process("/mail/activation",context);
        mailUtil.sendHtmlMail(user.getEmail(),"激活账号",content);
        return map;
    }

    public int activation(Integer id,String code){
        User user = userMapper.getUserById(id);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(id, 1);
            delRedisUser(id);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, Object> login(String username,String password,long expires) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isEmpty(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if (StringUtils.isEmpty(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        User user = userMapper.getUserByName(username);
        if (user==null){
            map.put("usernameMsg","账号不存在");
            return map;
        }
        if (user.getStatus()==0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }
        if (!user.getPassword().equalsIgnoreCase(MD5Utils.code(password+user.getSalt()))) {
            map.put("passwordMsg","账号或密码错误！");
            return map;
        }
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(user.getId());
        ticket.setTicket(MD5Utils.generateUUID());
        ticket.setStatus(0);
        ticket.setExpired(new Date(System.currentTimeMillis() + expires * 1000));
        String redisTicket = RedisKeyUtils.getTicket(ticket.getTicket());
        redisUtil.set(redisTicket,ticket,expires);
        map.put("ticket",ticket.getTicket());
        return map;
    }

    @Override
    public void logout(String ticket) {
        String redisTicket = RedisKeyUtils.getTicket(ticket);
        LoginTicket loginticket = (LoginTicket) redisUtil.get(redisTicket);
        loginticket.setStatus(1);
        redisUtil.set(redisTicket,loginticket);
    }

    @Override
    public LoginTicket findLoginTicket(String ticket) {
        String redisTicket = RedisKeyUtils.getTicket(ticket);
        return (LoginTicket) redisUtil.get(redisTicket);
    }

    @Override
    public Map<String, Object> updatePassword(User user,String old, String password) {
        Map map = new HashMap<>();
        if (StringUtils.isEmpty(old)){
            map.put("oldPassword","旧密码不能为空");
            return map;
        }
        if (!user.getPassword().equals(MD5Utils.code(old+user.getSalt()))){
            System.out.println(MD5Utils.code(old+user.getSalt()));
            map.put("oldPassword","密码错误");
            return map;
        }
        if (StringUtils.isEmpty(password)){
            map.put("newPassword","密码不能为空");
            return map;
        }
        if (password.length()<8){
            map.put("newPassword","密码长度不能小于8位!");
            return map;
        }
        userMapper.updatePassword(user.getId(),MD5Utils.code(password+user.getSalt()));
        delRedisUser(user.getId());
        return map;
    }

    @Override
    public User getUserByName(String name) {
        return userMapper.getUserByName(name);
    }

    @Override
    public boolean updateHeader(Integer id, String headerurl) {
        Boolean b=userMapper.updateHeader(id,headerurl);
        delRedisUser(id);
        return b;
    }

    //得到用户权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.getUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

    //优先从Redis缓存读取User
    private User getRedisUser(Integer userId){
        String userKey = RedisKeyUtils.getUserKey(userId);
        return (User) redisUtil.get(userKey);
    }
    //更新MySQL后删除Redis缓存中的User
    private void delRedisUser(Integer userId){
        String userKey = RedisKeyUtils.getUserKey(userId);
        redisUtil.del(userKey);
    }
    //取不到数据时初始化Redis缓存
    private User initRedisUser(Integer userId){
        User user = userMapper.getUserById(userId);
        String userKey = RedisKeyUtils.getUserKey(userId);
        redisUtil.set(userKey,user);
        return user;
    }
}
