package com.yupi.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * ClassName: RedissonConfig
 * Package: com.yupi.springbootinit.config
 * Description:
 *
 * @Author 张宽
 * @Create 2024/5/24 21:28
 * @Version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    @Bean
    public RedissonClient getRedissonClient(){
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(1)
                .setAddress("redis://127.0.0.1:6379")
                .setPassword(null);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;

    }
}
