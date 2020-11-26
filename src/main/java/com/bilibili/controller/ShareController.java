package com.bilibili.controller;

import com.bilibili.event.EventProducer;
import com.bilibili.pojo.Event;
import com.bilibili.utils.MD5Utils;
import com.bilibili.utils.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class ShareController implements UserStatus {
    @Autowired
    EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    @GetMapping("/share")
    @ResponseBody
    public String share(String htmlUrl){
        //随机生成文件名
        String fileName= MD5Utils.generateUUID();
        //创建生成长度的事件
        Event event=new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");
        eventProducer.fireEvent(event);
        //在界面显示长图的查看URL
        Map<String, Object> map=new HashMap<>();
        //在本地存储
        //map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        //在七牛云上存储
        map.put("shareUrl",shareBucketUrl+"/"+fileName);
        return MD5Utils.getJsonString("200",null,map);
    }

    @Deprecated
    @GetMapping("/share/image/{fileName}")
    @ResponseBody
    public void shareImage(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //在本地存储
        if (fileName.isEmpty()){
            throw new RuntimeException("文件名不能为空！");
        }
        response.setContentType("image/png");
        File file=new File(wkImageStorage+"\\"+fileName+".png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            log.error("获取长图失败: " + e.getMessage());
        }
    }
}
