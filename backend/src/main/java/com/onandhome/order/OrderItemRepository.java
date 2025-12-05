package com.onandhome.order;

import com.onandhome.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByProduct(com.onandhome.admin.adminProduct.entity.Product product);
    void deleteByProduct(com.onandhome.admin.adminProduct.entity.Product product);
}
