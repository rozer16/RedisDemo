package com.demo.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@NoArgsConstructor
@RedisHash(value="cg:{product}",timeToLive = 60)
public class Product {
    @Id
    private Integer id;
    private String name;
    private int quantity;
    private long price;
}