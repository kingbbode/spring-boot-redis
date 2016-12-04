package com.kingbbode.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by YG-MAC on 2016. 12. 4..
 */
@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, String> stringOperations;

    @Resource(name="redisTemplate")
    private ListOperations<String, String> listOperations;

    @Resource(name="redisTemplate")
    private HashOperations<String, String, String> hashOperations;

    @Resource(name="redisTemplate")
    private SetOperations<String, String> setOperations;
}


