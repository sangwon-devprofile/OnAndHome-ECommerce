package com.onandhome.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onandhome.admin.adminProduct.ProductRepository;
import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.cart.CartItemRepository;
import com.onandhome.cart.entity.CartItem;
import com.onandhome.notification.NotificationService;
import com.onandhome.order.dto.CreateOrderRequest;
import com.onandhome.order.dto.OrderDTO;
import com.onandhome.order.entity.Order;
import com.onandhome.order.entity.OrderItem;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final CartItemRepository cartRepo;
    private final NotificationService notificationService;

    // WebSocket 메시지 전송용 템플릿
    private final SimpMessagingTemplate messagingTemplate;

    // 관리자용 전체 주문 조회
    // 모든 주문을 createdAt 기준 내림차순으로 정렬하여 반환한다.
    // 관리자 페이지의 주문 관리 화면에서 사용됨.
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepo.findAll().stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 사용자의 주문 목록 조회
    // 마이페이지의 "주문 내역" 리스트를 불러오는 기능이다.
    // 숨김 처리된 주문(hidden = true)은 제외하고 가져온다.
    // 결과는 최신 주문 순서로 정렬되어 반환된다.
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrders(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return orderRepo.findByUserAndHiddenFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 주문 상세 조회
    // 사용자가 주문 내역에서 특정 주문을 클릭했을 때 사용하는 데이터.
    // 주문 정보 + 주문 상품 목록(OrderItem) + 결제/배송 상태 등을 포함해 DTO로 반환한다.
    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        return OrderDTO.fromEntity(order);
    }

    // 주문 생성 (결제 포함)
    // 결제 방식(카드/무통장)에 따라 상태가 ORDERED 또는 PAYMENT_PENDING으로 설정된다.
    // 주문 항목 생성 시 재고 차감이 이루어진다.
    // 주문 완료 시 사용자/관리자에게 WebSocket 알림이 전송된다.
    public OrderDTO createOrder(CreateOrderRequest request) {

        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 최소 1개 이상이어야 합니다.");
        }

        // 주문 항목 생성
        // 각 OrderItem 생성 시 상품 가격, 수량, 재고 차감 처리됨.
        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderRequest.OrderItemRequest itemReq : request.getOrderItems()) {

            Product product = productRepo.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

            if (itemReq.getQuantity() <= 0) {
                throw new IllegalArgumentException("주문 수량은 1 이상이어야 합니다.");
            }

            OrderItem orderItem = OrderItem.createOrderItem(
                    product,
                    product.getPrice(),
                    itemReq.getQuantity()
            );

            orderItems.add(orderItem);
        }

        // 결제 방식 설정 (기본: CARD)
        Order.PaymentMethod paymentMethod;
        try {
            String method = request.getPaymentMethod();
            paymentMethod = (method == null || method.isBlank())
                    ? Order.PaymentMethod.CARD
                    : Order.PaymentMethod.valueOf(method);
        } catch (Exception e) {
            paymentMethod = Order.PaymentMethod.CARD;
        }

        // 주문 엔티티 생성
        Order order = Order.create(user, orderItems, paymentMethod);

        // 배송 정보 저장
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingRequest(request.getShippingRequest());

        Order savedOrder = orderRepo.save(order);

        // 주문 완료 시 사용자 알림 (DB 저장 + 실시간 WebSocket)
        try {
            String productNames = orderItems.stream()
                    .limit(2)
                    .map(i -> i.getProduct().getName())
                    .collect(Collectors.joining(", "));

            if (orderItems.size() > 2) {
                productNames += " 외 " + (orderItems.size() - 2) + "건";
            }

            String messageText = "주문이 정상적으로 완료되었습니다. (" + productNames + ")";

            // DB 알림 저장
            notificationService.createNotification(
                    user.getUserId(),
                    "주문 완료",
                    messageText,
                    "ORDER",
                    savedOrder.getId(),
                    null
            );

            // WebSocket 1:1 개인 알림
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "ORDER");
            payload.put("orderId", savedOrder.getId());
            payload.put("title", "주문 완료");
            payload.put("message", messageText);
            payload.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSendToUser(
                    user.getUserId(),
                    "/queue/notifications",
                    payload
            );

        } catch (Exception e) {
            log.error("사용자 주문 완료 WebSocket 알림 전송 실패: {}", e.getMessage());
        }

        // 관리자 전체 방송(WebSocket + DB 알림)
        try {
            String productNames = orderItems.stream()
                    .limit(2)
                    .map(i -> i.getProduct().getName())
                    .collect(Collectors.joining(", "));

            if (orderItems.size() > 2) {
                productNames += " 외 " + (orderItems.size() - 2) + "건";
            }

            String adminText = "새로운 주문이 등록되었습니다. 구매자: "
                    + user.getUsername() + " (" + productNames + ")";

            // DB 저장
            notificationService.createAdminNotification(
                    "새 주문 등록",
                    adminText,
                    "ADMIN_ORDER",
                    savedOrder.getId()
            );

            // WebSocket 관리자 브로드캐스트
            Map<String, Object> adminPayload = new HashMap<>();
            adminPayload.put("type", "ADMIN_ORDER");
            adminPayload.put("orderId", savedOrder.getId());
            adminPayload.put("title", "새 주문 등록");
            adminPayload.put("message", adminText);
            adminPayload.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend(
                    "/topic/admin-notifications",
                    adminPayload
            );

        } catch (Exception e) {
            log.error("관리자 주문 WebSocket 알림 전송 실패: {}", e.getMessage());
        }

        return OrderDTO.fromEntity(savedOrder);
    }

    // 장바구니 기반 주문 생성
    // 장바구니에서 상품을 가져와 OrderItem으로 변환하고 주문으로 생성한다.
    // 주문 생성 후 장바구니는 비워진다.
    public OrderDTO createOrderFromCart(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        List<CartItem> cartItems = cartRepo.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어 있습니다.");
        }

        List<OrderItem> orderItems = cartItems.stream()
                .map(c -> OrderItem.createOrderItem(
                        c.getProduct(),
                        c.getProduct().getPrice(),
                        c.getQuantity()
                ))
                .collect(Collectors.toList());

        Order order = Order.create(user, orderItems, Order.PaymentMethod.CARD);
        Order saved = orderRepo.save(order);

        cartRepo.deleteByUser(user);
        return OrderDTO.fromEntity(saved);
    }

    // 주문 결제 처리
    // 결제 성공 시 상태가 ORDERED로 변경되고 결제시간이 기록된다.
    public OrderDTO pay(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.pay();
        return OrderDTO.fromEntity(orderRepo.save(order));
    }

    // 무통장입금 확인 등 결제 승인 처리
    public OrderDTO confirmPayment(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.confirmPayment();
        return OrderDTO.fromEntity(orderRepo.save(order));
    }

    // 주문 취소
    // 주문 상태가 배송완료 전이면 취소 가능,
    // OrderItem 내부에서 재고 복구 처리됨.
    public OrderDTO cancel(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.cancel();
        return OrderDTO.fromEntity(orderRepo.save(order));
    }

    // 주문 배송 상태 조회
    // ORDERED, DELIVERING, DELIVERED, CANCELED 상태를 텍스트로 반환한다.
    @Transactional(readOnly = true)
    public String track(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        return switch (order.getStatus()) {
            case ORDERED -> "주문 완료";
            case DELIVERING -> "배송중";
            case DELIVERED -> "배송완료";
            case CANCELED -> "주문 취소";
            default -> "알 수 없음";
        };
    }

    // checkout 시 장바구니 조회용
    @Transactional(readOnly = true)
    public List<CartItem> checkout(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return cartRepo.findByUser(user);
    }

    // 관리자 검색 기능
    // 상품명, 사용자명, 사용자 ID를 기준으로 검색 필터링 수행.
    @Transactional(readOnly = true)
    public List<OrderDTO> search(String keyword) {
        return orderRepo.findAll().stream()
                .filter(order -> {
                    boolean matchProduct = order.getOrderItems() != null
                            && !order.getOrderItems().isEmpty()
                            && order.getOrderItems().get(0).getProduct() != null
                            && order.getOrderItems().get(0).getProduct().getName() != null
                            && order.getOrderItems().get(0).getProduct().getName().contains(keyword);

                    boolean matchUsername = order.getUser() != null
                            && order.getUser().getUsername() != null
                            && order.getUser().getUsername().contains(keyword);

                    boolean matchUserId = order.getUser() != null
                            && order.getUser().getUserId() != null
                            && order.getUser().getUserId().contains(keyword);

                    return matchProduct || matchUsername || matchUserId;
                })
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 주문 숨김 처리
    // 마이페이지에서 보이지 않도록 hidden 값을 true로 변경.
    @Transactional
    public void hideOrder(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.hide();
        orderRepo.save(order);
    }

    // 주문 숨김 해제
    @Transactional
    public void unhideOrder(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));
        order.unhide();
        orderRepo.save(order);
    }
}

/*
요약
1. 주문 생성 시 사용자와 관리자에게 DB + WebSocket 알림이 동시에 처리된다.
2. 사용자 알림은 convertAndSendToUser()를 통해 1:1 개인 큐로 전달된다.
3. 관리자 알림은 /topic/admin-notifications 로 전체 브로드캐스트된다.
4. 주문 조회 기능(getOrders, getOrder)은 마이페이지 데이터를 구성하는 핵심 메서드이다.
5. 숨김 주문(hidden)은 사용자 조회에서 제외되고, unhide로 다시 표시 가능하다.
*/
