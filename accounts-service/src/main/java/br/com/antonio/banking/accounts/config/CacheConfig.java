package br.com.antonio.banking.accounts.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/**
 * Redis cache configuration.
 *
 * Cache strategy per use case:
 * - account-balance: 30s TTL (balance changes frequently)
 * - account-data: 5min TTL (account info changes rarely)
 *
 * Cache is disabled in test profile (application-test.yml sets
 * spring.cache.type: none to avoid Redis dependency in unit tests).
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        var jsonSerializer = RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer());

        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(jsonSerializer)
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(Map.of(
                        "account-balance", defaultConfig.entryTtl(Duration.ofSeconds(30)),
                        "account-data",    defaultConfig.entryTtl(Duration.ofMinutes(5))
                ))
                .build();
    }
}