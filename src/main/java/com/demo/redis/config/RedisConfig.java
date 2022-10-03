package com.demo.redis.config;

import com.demo.redis.model.Product;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.MappingConfiguration;
import org.springframework.data.redis.core.index.IndexConfiguration;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.File;
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

    @Value("${redis.username}")
    private String username;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.sslKeystoreLocation}")
    private String keyStoreLocation;

    @Value("${redis.sslKeystorePassword}")
    private String keyStorePasword;

    @Value("${redis.sslTruststoreLocation}")
    private String trustStoreLocation;

    @Value("${redis.sslTruststorePassword}")
    private String trustStorePasword;

    @Value("${redis.isSslEnabled}")
    private boolean isSslEnabled;

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
        if(username != null && username.length() > 0 && password != null && password.length() > 0) {
            redisStandaloneConfiguration.setUsername(username);
            redisStandaloneConfiguration.setPassword(password);
        }
        return redisStandaloneConfiguration;

    }
    private SslOptions getSSLOption(){
           return SslOptions.builder().
                truststore(new File(trustStoreLocation),trustStorePasword)
                .keystore(new File(keyStoreLocation),password.toCharArray())
                .build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration configuration = redisStandaloneConfiguration();


        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(100);
        poolConfig.setMinIdle(50);

        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestOnCreate(false);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setEvictorShutdownTimeout(Duration.ofMillis(-1));
        poolConfig.setSoftMinEvictableIdleTime(Duration.ofMillis(-1));
        poolConfig.setSoftMinEvictableIdleTime(Duration.ofMillis(-1));

        poolConfig.setLifo(true);
        poolConfig.setFairness(false);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWait(Duration.ofMillis(-1));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(1000));
        poolConfig.setNumTestsPerEvictionRun(-5);

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder
                clientConfigurationBuilder =
                LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(200))
                .poolConfig(poolConfig);

        ClientOptions.Builder clientOptionBuilder = ClientOptions.builder().autoReconnect(true);
        if(isSslEnabled){
            clientOptionBuilder = clientOptionBuilder.sslOptions(this.getSSLOption());
            clientConfigurationBuilder = clientConfigurationBuilder.useSsl().and();
        }

        LettuceClientConfiguration clientConfiguration =
                clientConfigurationBuilder.clientOptions(clientOptionBuilder.build()).build();
        LettuceConnectionFactory connectionFactory =
                new LettuceConnectionFactory(configuration,clientConfiguration);
        connectionFactory.afterPropertiesSet();
        return connectionFactory;


    }

    //@Bean
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