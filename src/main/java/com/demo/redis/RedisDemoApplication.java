package com.demo.redis;

import com.demo.redis.model.Product;
import com.demo.redis.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.Map;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class
})
@Slf4j
public class RedisDemoApplication implements CommandLineRunner {

    @Autowired
    private RedisTemplate<byte[], byte[]> redisTemplate;

    @Autowired
    private ProductService productService;


    public static void main(String[] args) {
        SpringApplication.run(RedisDemoApplication.class, args);
    }



    @Override
    public void run(String... args) throws Exception {

        RedisConnection con = redisTemplate.getConnectionFactory().getConnection();
        System.out.println(Arrays.toString(con.get("name".getBytes())));
        System.out.println("=============");

        Product p = new Product();
        p.setId(4);
        p.setName("prod3");
        p.setQuantity(101);
        p.setPrice(10001);
        productService.saveProduct(p);

        Product p1 = new Product();
        p1.setId(5);
        p1.setName("prod4");
        p1.setQuantity(101);
        p1.setPrice(10001);
        productService.saveProduct(p1);
        log.info("Product saved successfully!!");
        log.info(productService.findAllProduct().toString());
    }
}
