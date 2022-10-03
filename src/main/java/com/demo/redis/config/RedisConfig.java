package com.demo.redis.config;

import com.demo.redis.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.MappingConfiguration;
import org.springframework.data.redis.core.index.IndexConfiguration;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableRedisRepositories
@ComponentScan("com.demo.redis.repository")
@PropertySource("classpath:env.properties")
@Slf4j
public class RedisConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.hash}")
    private String hash;

    @Value("${redis.keyspace}")
    private String keyspace;

    @Value("${redis.timeToLive}")
    private long timeToLive;

    @Value("${redis.timeout}")
    private long timeout;

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
        return redisStandaloneConfiguration;

    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(){
        JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfigurationBuilder= JedisClientConfiguration.builder();
        jedisClientConfigurationBuilder.connectTimeout(Duration.ofMillis(timeout));
        jedisClientConfigurationBuilder.readTimeout(Duration.ofMillis(timeout));
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration(),jedisClientConfigurationBuilder.build());
        jedisConnectionFactory.afterPropertiesSet();
        return jedisConnectionFactory;
    }


    @Bean
    public RedisTemplate<? ,?> redisTemplate(){
        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new JdkSerializationRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public KeyspaceConfiguration keyspaceConfiguration(){
        log.info("Hash is ==> "+hash+"_"+Product.class.getSimpleName());
        KeyspaceConfiguration kc = new KeyspaceConfiguration();
        kc.addKeyspaceSettings(new KeyspaceConfiguration.KeyspaceSettings(Product.class, getKey(Product.class.getSimpleName())));
        return kc;
    }

    @Bean
    public RedisMappingContext keyValueMappingContext(){
        return new RedisMappingContext(new MappingConfiguration(new IndexConfiguration(),keyspaceConfiguration()));
    }


    private String getKey(String className){
        StringBuffer sb = new StringBuffer();
        if(keyspace != null && keyspace.length()>0)
                sb.append(keyspace);
        if(hash != null && hash.length() > 0)
            sb.append(hash+"_");
        sb.append(className);
        log.info("Key is ===> "+sb.toString());
        return sb.toString();
    }
}