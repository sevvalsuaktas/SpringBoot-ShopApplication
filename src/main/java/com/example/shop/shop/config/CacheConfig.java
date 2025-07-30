package com.example.shop.shop.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching   // caching’i uygulamada etkinleştirmek için
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // cache ömrü
                .disableCachingNullValues() // null değerleri saklama
                .prefixCacheNameWith("shop::"); // tüm cache anahtarlarına prefix

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }
}