package com.bilibili;

import com.bilibili.mapper.DiscussPostMapper;
import com.bilibili.pojo.DiscussPost;
import com.bilibili.service.DiscussPostService;
import com.bilibili.service.ElasticsearchService;
import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class CommunityApplicationTests {
    @Autowired
    ElasticsearchService elasticsearchService;
    @Autowired
    DiscussPostMapper discussPostMapper;

    @Test
    public void test(){
        List<DiscussPost> list  = discussPostMapper.findAllDiscussPost(0);
        System.out.println(list.size());
        for (DiscussPost discussPost : list) {
            elasticsearchService.saveDiscussPost(discussPost);
        }
    }
}
