package com.bilibili.mapper;

import com.bilibili.pojo.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticsearchMapper extends ElasticsearchRepository<DiscussPost, Integer> {
}
