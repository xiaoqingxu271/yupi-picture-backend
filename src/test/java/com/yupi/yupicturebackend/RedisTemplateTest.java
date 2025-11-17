package com.yupi.yupicturebackend;

import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
* @author chun0
* @since 2025/11/17 18:10
* @version 1.0
*/
@SpringBootTest
public class RedisTemplateTest {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void test() {
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        String key = "testKey";
        String value = "testValue";
        // 测试添加操作
        opsForValue.set(key, value);
        String expiredValue = opsForValue.get(key);
        Assertions.assertEquals(value, expiredValue, "获取到的值与添加的值不一致");
        // 测试修改操作
        String newValue = "newValue";
        opsForValue.set(key, newValue);
        String newExpiredValue = opsForValue.get(key);
        Assertions.assertEquals(newValue, newExpiredValue, "获取到的值与修改后的值不一致");
        // 测试查询操作
        String searchValue = opsForValue.get(key);
        Assertions.assertEquals(newValue, searchValue, "查询到的值与修改后的值不一致");
        // 测试删除操作
        stringRedisTemplate.delete(key);
        String deletedValue = opsForValue.get(key);
        Assertions.assertNull(deletedValue, "删除后的值应该为null");
    }
}
