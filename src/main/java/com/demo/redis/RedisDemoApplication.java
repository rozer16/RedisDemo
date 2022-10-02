package com.demo.redis;

import com.demo.redis.model.Product;
import com.demo.redis.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        RedisAutoConfiguration.class
})
public class RedisDemoApplication implements CommandLineRunner {

    @Autowired
    ProductRepository productRepository;


    public static void main(String[] args) {
        SpringApplication.run(RedisDemoApplication.class, args);
    }



    @Override
    public void run(String... args) throws Exception {
        Product p = new Product();
        p.setId(4);
        p.setName("prod3");
        p.setQuantity(101);
        p.setPrice(10001);
        productRepository.save(p);
        System.out.println("Product saved successfully!!");
        Product p1 = new Product();
        p1.setId(3);
        productRepository.delete(p1);
        System.out.println(productRepository.findAll());



    }
}
