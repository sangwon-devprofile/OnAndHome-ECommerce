package com.onandhome.order.entity;

import com.onandhome.admin.adminProduct.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private int orderPrice; // 주문 가격

    @Column(nullable = false)
    private int count; //주문 수량

    //생성 매소드
    public static OrderItem createOrderItem(Product product, int orderPrice, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.product = product;
        orderItem.orderPrice = orderPrice;
        orderItem.count = count;

        product.removeStock(count);
        return orderItem;
    }

    //비즈니스로직
    /**
     * 주문 취소 시 재고 원복
     */
    public void cancel() {
        getProduct().addStock(count); // 재고 원복 로직
    }

    /**
     * 주문 상품 전체 가격 조회
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }

    //== 연관관계 편의 메소드 ==//
    protected void setOrder(Order order) {
        this.order = order;
    }
}
