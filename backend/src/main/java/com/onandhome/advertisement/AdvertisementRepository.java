package com.onandhome.advertisement;

import com.onandhome.advertisement.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    
    List<Advertisement> findAllByOrderByCreatedAtDesc();
    
    List<Advertisement> findByActiveTrueOrderByCreatedAtDesc();
}
