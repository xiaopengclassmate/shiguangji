package com.foodtime.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 * 用于延迟队列、分布式锁等高级Redis功能
 */
@Configuration
public class RedissonConfig {

    @Value("${foodtime.redis.host}")
    private String host;

    @Value("${foodtime.redis.port}")
    private int port;

    @Value("${foodtime.redis.database}")
    private int database;

    @Value("${foodtime.redis.password}")
    private String password;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database)
                .setPassword(password)
                .setConnectionPoolSize(32)
                .setConnectionMinimumIdleSize(8);
        return Redisson.create(config);
    }
}
