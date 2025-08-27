package com.example.shop.shop.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory factory,
            @Value("${spring.cache.redis.time-to-live:600000}") Duration ttl
    ) {

        RedisSerializationContext.SerializationPair<String> keyPair =
                RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer());

        RedisSerializationContext.SerializationPair<Object> valuePair =
                RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisSerializer.json());

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .prefixCacheNameWith("shop::")
                .serializeKeysWith(keyPair)
                .serializeValuesWith(valuePair);

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }
}