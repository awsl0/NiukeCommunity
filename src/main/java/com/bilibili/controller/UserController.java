package com.bilibili.controller;


import com.bilibili.annotation.LoginRequired;
import com.bilibili.pojo.User;
import com.bilibili.service.FollowService;
import com.bilibili.service.LikeService;
import com.bilibili.service.UserService;
import com.bilibili.utils.HostUtils;
import com.bilibili.utils.MD5Utils;
import com.bilibili.utils.UserStatus;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LiXiang
 * @since 2020-10-12
 */
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController implements UserStatus{
    @Autowired
    UserService userService;
    @Autowired
    LikeService likeService;
    @Autowired
    FollowService followService;

    @Value("${community.path.upload}")
    private String upload;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @GetMapping("/setting")
    public String settingPage(Model model){
        //上传文件名
        String fileName = MD5Utils.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", MD5Utils.getJsonString("200"));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);
        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "site/setting";
    }

    // 更新头像路径
    @PostMapping("/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return MD5Utils.getJsonString("403", "文件名不能为空!");
        }
        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(HostUtils.getUser().getId(), url);

        return MD5Utils.getJsonString("200");
    }


    @LoginRequired
    @PostMapping("/upload")
    @Deprecated
    public String upload(MultipartFile headerImage, Model model) {
        if (headerImage==null){
            model.addAttribute("error","您还没有选择照片");
            return "site/setting";
        }
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        filename=MD5Utils.generateUUID()+suffix;
        File dest=new File(upload+"/"+filename);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            log.info(e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }
        User user = HostUtils.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    @GetMapping("/header/{filename}")
    @Deprecated
    public void header(@PathVariable String filename, HttpServletResponse response){
        String uploadPath=upload+"/"+filename;
        String suffix = filename.substring(filename.lastIndexOf("."));
        response.setContentType("image/"+suffix);
        try {
            FileInputStream fis = new FileInputStream(uploadPath);
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("获取头像失败"+e.getMessage());
        }
    }

    @PostMapping("/password")
    @LoginRequired
    public String updatePassword(String oldPassword,String newPassword,Model model){
        User user = HostUtils.getUser();
        Map map =userService.updatePassword(user,oldPassword,newPassword);
        if (map.isEmpty()){
            return "redirect:/logout";
        }
        model.addAttribute("oldPassword",map.get("oldPassword"));
        model.addAttribute("newPassword",map.get("newPassword"));
        return "site/setting";
    }

    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") Integer userId,Model model){
        User user = userService.getUserById(userId);
        if (user==null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user",user);
        long userLikeCount = likeService.userLikeCount(userId);
        model.addAttribute("userLikeCount",userLikeCount);
        //关注
        long followeeCount = followService.FolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝
        long followersCount = followService.FollowersCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followersCount",followersCount);
        boolean isFollowed = false;
        if (HostUtils.getUser() != null) {
            isFollowed = followService.isFollow(HostUtils.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("isFollowed", isFollowed);
        return "site/profile";
    }
}
