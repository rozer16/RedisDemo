package com.demo.redis.service;

import com.demo.redis.model.Product;
import com.demo.redis.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ProductService {
    @Autowired
    ProductRepository productRepository;


    public Product saveProduct(Product p){
        productRepository.save(p);
        return p;
    }

    public List<Product> findAllProduct(){
        return productRepository.findAll();
    }

    public void deleteProduct(int id){
        Product p = productRepository.findById(id).get();
        productRepository.delete(p);
    }

    public Product getProductById(int id){
        return productRepository.findById(id).get();
    }

}


