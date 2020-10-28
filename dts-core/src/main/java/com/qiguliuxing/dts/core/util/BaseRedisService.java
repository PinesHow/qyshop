package com.qiguliuxing.dts.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author AoZB
 * @description Redis Cache
 * @date 2019/5/15
 * @updateAuthor mh
 * @update 2020/4/14
 */
@Component
public class BaseRedisService {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public BaseRedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    //添加缓存
    public void setString(String key,Object data) { setString(key,data,null);}

    //添加缓存  设置过期时间
    public void setString(String key,Object data,Long timeout){
        if(data instanceof String){
            String value = (String)data;
            redisTemplate.opsForValue().set(key,value);
        }
        if (null != timeout){
            redisTemplate.expire(key,timeout,TimeUnit.SECONDS);
        }
    }
    public Set<String> getKeySet(String pattern){
//            redisTemplate.opsForValue().setO(key,data);
//        if (0 != timeout){
//            redisTemplate.expire(key,timeout,TimeUnit.SECONDS);
//        }
        return redisTemplate.keys(pattern);

    }
//    public void setValue(String key, Object data){
//        setValue(key,data,0);
//    }

    //根据key获取value值
    public String getString(String key){
        return (String)redisTemplate.opsForValue().get(key);
    }

    public Object getValue(String key){
       return  redisTemplate.opsForValue().get(key);
    }
    //根据key删除缓存 (key,value)
    public void delKey(String key){
        redisTemplate.delete(key);
    }

    //判断key是否存在
    public boolean hasKey(String key){
       return redisTemplate.hasKey(key);
    }

    public void setRange(){

    }
}
