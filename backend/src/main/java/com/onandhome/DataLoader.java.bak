package com.onandhome;

import com.onandhome.product.ProductRepository;
import com.onandhome.product.entity.Product;
import com.onandhome.user.entity.User;
import com.onandhome.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class DataLoader implements CommandLineRunner {
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    public DataLoader(ProductRepository productRepo, UserRepository userRepo){
        this.productRepo = productRepo; this.userRepo = userRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        productRepo.save(new Product(null, "스마트폰 A", "좋은 스마트폰", 800000, 10));
        productRepo.save(new Product(null, "노트북 B", "고성능 노트북", 1200000, 5));
        productRepo.save(new Product(null, "무선이어폰 C", "음질좋음", 150000, 20));

    }
}
