package com.bilibili.controller;

import com.bilibili.pojo.DiscussPost;
import com.bilibili.pojo.User;
import com.bilibili.service.DiscussPostService;
import com.bilibili.service.ElasticsearchService;
import com.bilibili.service.LikeService;
import com.bilibili.service.UserService;
import com.bilibili.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class HomeController implements UserStatus {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;
    @Autowired
    LikeService likeService;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ElasticsearchService elasticsearchService;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @GetMapping({"/","/index"})
    public String index(Model model,@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum,@RequestParam(defaultValue = "0",name = "orderMode")Integer orderMode){
        List<Map<String, Object>> list = new ArrayList<>();
        PageInfo page = discussPostService.findAllDiscussPost(0,pageNum,15,orderMode);
        List<DiscussPost> pageList = page.getList();
        if (pageList!=null){
            for (DiscussPost post : pageList) {
                Map map=new HashMap<String, Object>();
                map.put("post",post);
                User user = userService.getUserById(Integer.valueOf(post.getUserId()));
                map.put("user",user);
                long likeCount = likeService.LikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount",likeCount);
                int likeStatus=HostUtils.getUser()==null?0:likeService.LinkStatus(HostUtils.getUser().getId(),ENTITY_TYPE_POST,post.getId());
                map.put("likeStatus",likeStatus);
                list.add(map);
            }
        }
        model.addAttribute("list",list);
        model.addAttribute("page",page);
        model.addAttribute("orderMode",orderMode);
        model.addAttribute("path","/index?orderMode="+orderMode);
        return "index";
    }

    @GetMapping("/register")
    public String registerPage(){
        return "site/register";
    }

    @PostMapping("/register")
    public String register(User user,Model model){
        Map<String, Object> map = userService.register(user);
        if (map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发了一封激活邮件，请尽快激活");
            model.addAttribute("url","/index");
            return "site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("user",user);
            return "site/register";
        }
    }

    @GetMapping("/activation/{id}/{code}")
    public String activation(@PathVariable("id") Integer id,@PathVariable("code") String code,Model model){
        int activation = userService.activation(id, code);
        if (activation == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("url", "/login");
        } else if (activation == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("url", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("url", "/index");
        }
        return "site/operate-result";
    }

    @GetMapping("/login")
    public String LoginPage(){
        return "site/login";
    }

    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response){
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        String owner = MD5Utils.generateUUID();
        Cookie cookie = new Cookie("owner",owner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        String kaptchaKey = RedisKeyUtils.getKaptchaKey(owner);
        redisUtil.set(kaptchaKey,text,60);
        response.setContentType("image/png");
        try {
            OutputStream stream = response.getOutputStream();
            ImageIO.write(image,"png",stream);
        }catch (Exception e){
            log.info(e.toString());
        }
    }

    @PostMapping("/login")
    public String login(Model model,String username,String password,String kaptcha,HttpServletResponse response,
                        boolean remember,@CookieValue("owner") String owner){
        String redisKaptcha = null;
        if (!StringUtils.isEmpty(owner)) {
            String kaptchaKey = RedisKeyUtils.getKaptchaKey(owner);
            redisKaptcha= (String) redisUtil.get(kaptchaKey);
        }
        if (!redisKaptcha.equalsIgnoreCase(kaptcha)|| StringUtils.isEmpty(redisKaptcha)||StringUtils.isEmpty(kaptcha)){
            model.addAttribute("kaptchaMsg","验证码错误");
            return "site/login";
        }
        int time=remember?UserStatus.REMEMBER_EXPIRED_SECONDS:UserStatus.DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, time);
        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setMaxAge(time);
            cookie.setPath(contextPath);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue(value = "ticket")String ticket){
        userService.logout(ticket);
        return "redirect:/login";
    }

    @GetMapping("/search")
    public String search(@RequestParam(defaultValue ="",value = "keyword")String keyword,Model model,@RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        if (keyword.isEmpty()){
            return "redirect:/index";
        }
        Page<DiscussPost> searchResult = elasticsearchService.searchDiscussPost(keyword, pageNum, 15);
        List<Map<String, Object>> list = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.getUserById(Integer.valueOf(post.getUserId())));
                // 点赞数量
                map.put("likeCount", likeService.LikeCount(ENTITY_TYPE_POST, post.getId()));
                list.add(map);
            }
        }
        model.addAttribute("list", list);
        model.addAttribute("keyword", keyword);
        com.github.pagehelper.Page page = PageHelper.startPage(pageNum, 15);
        page.setTotal(searchResult.getTotalElements());
        model.addAttribute("page",page);
        model.addAttribute("path","/search?keyword="+keyword);
        return "site/search";
    }

    @GetMapping("/error")
    public String error(){
        return "error/500";
    }

    @GetMapping("/denied")
    public String denied(){
        return "error/404";
    }
}
