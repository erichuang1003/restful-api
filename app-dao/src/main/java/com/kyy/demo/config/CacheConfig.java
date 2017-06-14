package com.kyy.demo.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.google.common.cache.CacheBuilder;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

	private static Logger logger = LoggerFactory.getLogger(CacheConfig.class);

	public static final String CACHE_REMOTE = "cache_remote";
	public static final String CACHE_LOCAL = "cache_local";

	@Value("${cache.redis.nodes}")
	private List<String> nodes;

	@Value("${cache.redis.timeout}")
	private int timeout;

	@Value("${cache.redis.maxRedirects}")
	private int maxRedirects;

	@Value("${cache.redis.password}")
	private String password;

	@Value("${cache.redis.ttl}")
	private int ttl;

	@Value("${cache.redis.maxIdle}")
	private int maxIdle;

	@Value("${cache.redis.maxWait}")
	private int maxWait;

	@Value("${cache.redis.maxTotal}")
	private int maxTotal;

	@Value("${cache.redis.testOnBorrow}")
	private boolean testOnBorrow;

	@Value("${cache.local.maxSize}")
	private int localMaxSize;

	@Value("${cache.local.ttl}")
	private int localTtl;

	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(maxIdle);
		poolConfig.setMaxWaitMillis(maxWait);
		poolConfig.setMaxTotal(maxTotal);
		poolConfig.setTestOnBorrow(testOnBorrow);

		RedisClusterConfiguration conf = new RedisClusterConfiguration(nodes);
		conf.setMaxRedirects(maxRedirects);
		JedisConnectionFactory factory = new JedisConnectionFactory(conf, poolConfig);
		factory.setTimeout(timeout);
		factory.setPassword(password);
		return factory;
	}

	@Bean
	public RedisSerializer<Object> jacksonSerializer() {
		return new GenericJackson2JsonRedisSerializer();
	}

	@Bean
	public <K, V> RedisTemplate<K, V> redisTemplate() {
		RedisTemplate<K, V> redisTemplate = new RedisTemplate<K, V>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setDefaultSerializer(jacksonSerializer());
		redisTemplate.setConnectionFactory(jedisConnectionFactory());
		return redisTemplate;
	}

	@Bean
	public RedisCache redisCache() {
		return new RedisCache(CACHE_REMOTE, "_".getBytes(), redisTemplate(), ttl);
	}

	@Bean
	public GuavaCache guavaCache() {
		return new GuavaCache(CACHE_LOCAL, CacheBuilder.newBuilder().recordStats().maximumSize(localMaxSize)
				.expireAfterWrite(localTtl, TimeUnit.MILLISECONDS).build());
	}

	@Bean
	@Override
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		ArrayList<Cache> caches = new ArrayList<Cache>();
		caches.add(redisCache());
		caches.add(guavaCache());
		cacheManager.setCaches(caches);
		return cacheManager;
	}

	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object o, Method method, Object... objects) {
				StringBuilder sb = new StringBuilder();
				boolean isAppend = false;
				for (Object obj : objects) {
					if (isAppend) {
						sb.append('_');
					}
					isAppend = true;
					if (obj instanceof Class) {
						sb.append(((Class<?>) obj).getName());
					} else {
						sb.append(obj.toString());
					}
				}
				return sb.toString();
			}
		};
	}

	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		return new CacheErrorHandler() {

			@Override
			public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
				logger.warn("cache put error {} {} {} {}", cache.getName(), key, exception.getClass().getName(),
						exception.getMessage());
			}

			@Override
			public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
				logger.warn("cache get error {} {} {} {}", cache.getName(), key, exception.getClass().getName(),
						exception.getMessage());
			}

			@Override
			public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
				logger.warn("cache evict error {} {} {} {}", cache.getName(), key, exception.getClass().getName(),
						exception.getMessage());
			}

			@Override
			public void handleCacheClearError(RuntimeException exception, Cache cache) {
				logger.warn("cache clear error {} {} {}", cache.getName(), exception.getClass().getName(),
						exception.getMessage());
			}
		};
	}

}
