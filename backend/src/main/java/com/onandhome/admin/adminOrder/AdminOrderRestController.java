package com.onandhome.admin.adminOrder;

import com.onandhome.order.dto.OrderDTO;
import com.onandhome.order.dto.OrderItemDTO;
import com.onandhome.order.entity.Order;
import com.onandhome.order.entity.OrderItem;
import com.onandhome.order.OrderRepository;
import com.onandhome.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// 관리자 전용 주문 관리 REST API
// 결제 상태 확인, 배송 상태 변경, 주문 상세 조회 등 백오피스 기능을 담당한다.
@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderRestController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    // 전체 주문 목록 조회 (관리자용)
    // 주문 상태와 관계없이 모든 주문을 조회한다.
    // ORDERED, PAYMENT_PENDING, DELIVERING, CANCELED 등 전체 상태 확인 가능.
    @Transactional(readOnly = true)
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        log.info("=== 관리자 주문 목록 조회 ===");

        try {
            List<Order> orderList = orderRepository.findAll();

            // 엔티티 전체를 DTO로 변환하여 관리자 화면에서 사용할 수 있도록 구조화
            List<OrderDTO> orderDTOList = orderList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("주문 목록 조회 성공 - 총 {}개", orderDTOList.size());
            return ResponseEntity.ok(orderDTOList);
        } catch (Exception e) {
            log.error("주문 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    // 관리자용 주문 상세 조회
    // 결제 여부(paidAt), 주문 상태, 배송 정보, 주문 상품 구성 등을 확인할 때 사용된다.
    // PAYMENT_PENDING 상태의 무통장 주문도 여기서 상세 확인 가능.
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderDetail(@PathVariable Long id) {
        log.info("=== 관리자 주문 상세 조회: {} ===", id);

        try {
            Optional<Order> orderOptional = orderRepository.findById(id);

            if (orderOptional.isEmpty()) {
                log.warn("주문을 찾을 수 없음: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            OrderDTO orderDTO = convertToDTO(orderOptional.get());
            log.info("주문 상세 조회 성공: {}", id);
            return ResponseEntity.ok(orderDTO);
        } catch (Exception e) {
            log.error("주문 상세 조회 실패: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 주문 상태 변경 (관리자용)
    // 관리자가 직접 ORDERED, DELIVERING, DELIVERED, CANCELED 등을 변경한다.
    // 무통장 입금(BANK_TRANSFER) 경우 PAYMENT_PENDING → ORDERED 변경 전에 사용됨.
    @Transactional
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        log.info("=== 관리자 주문 상태 변경: {} ===", id);

        Map<String, Object> response = new HashMap<>();

        try {
            String statusStr = request.get("status");

            // 상태 값 검증
            if (statusStr == null || statusStr.isEmpty()) {
                response.put("success", false);
                response.put("message", "상태 값이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Order> orderOptional = orderRepository.findById(id);

            if (orderOptional.isEmpty()) {
                response.put("success", false);
                response.put("message", "주문을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Order order = orderOptional.get();

            try {
                // 문자열을 OrderStatus Enum으로 변환
                Order.OrderStatus newStatus = Order.OrderStatus.valueOf(statusStr);

                order.setStatus(newStatus);
                orderRepository.save(order);

                response.put("success", true);
                response.put("message", "주문 상태가 변경되었습니다.");
                response.put("status", newStatus.name());
                return ResponseEntity.ok(response);

            } catch (IllegalArgumentException e) {
                response.put("success", false);
                response.put("message", "유효하지 않은 상태 값입니다: " + statusStr);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("주문 상태 변경 실패: {}", id, e);
            response.put("success", false);
            response.put("message", "주문 상태 변경 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 무통장 입금 확인 (관리자용)
    // PAYMENT_PENDING → ORDERED 로 전환됨.
    // paidAt 필드도 이 과정에서 자동 기록된다.
    // 결제 방식이 BANK_TRANSFER인 경우에만 정상적으로 처리된다.
    @Transactional
    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<Map<String, Object>> confirmPayment(@PathVariable Long id) {
        log.info("=== 무통장 입금 확인: {} ===", id);

        Map<String, Object> response = new HashMap<>();

        try {
            OrderDTO orderDTO = orderService.confirmPayment(id);

            response.put("success", true);
            response.put("message", "입금이 확인되었습니다.");
            response.put("order", orderDTO);

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage()); // 상태 조건 불일치 등
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("입금 확인 중 오류: {}", id, e);
            response.put("success", false);
            response.put("message", "입금 확인 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Order 엔티티 → OrderDTO 변환
    // 관리자 주문 상세 페이지에서 주문 내용을 구성하는 데이터 구조이다.
    // 사용자 정보, 배송 정보, 주문 항목, 결제 상태 등을 모두 담아 반환한다.
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();

        dto.setId(order.getId());
        dto.setOrderId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus().name());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaidAt(order.getPaidAt());

        // 배송 정보
        dto.setRecipientName(order.getRecipientName());
        dto.setRecipientPhone(order.getRecipientPhone());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingRequest(order.getShippingRequest());
        dto.setAddress(order.getShippingAddress());
        dto.setDeliveryMessage(order.getShippingRequest());

        // 사용자 정보
        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getUserId());
            dto.setUsername(order.getUser().getUsername());
            dto.setPhone(order.getUser().getPhone());
            dto.setEmail(order.getUser().getEmail());
            dto.setUserEmail(order.getUser().getEmail());
            dto.setUserPhone(order.getUser().getPhone());
            dto.setUserAddress(order.getUser().getAddress());
        }

        // 주문 상품 정보(OrderItem)
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                    .map(this::convertOrderItemToDTO)
                    .collect(Collectors.toList());
            dto.setOrderItems(orderItemDTOs);
        } else {
            dto.setOrderItems(List.of());
        }

        return dto;
    }

    // OrderItem 엔티티 → OrderItemDTO 변환
    // 관리자 주문 상세에서 개별 상품 구성 확인에 사용되는 데이터.
    private OrderItemDTO convertOrderItemToDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();

        dto.setId(orderItem.getId());
        dto.setOrderItemId(orderItem.getId());
        dto.setQuantity(orderItem.getCount());
        dto.setOrderPrice(orderItem.getOrderPrice());
        dto.setPrice(orderItem.getOrderPrice());

        if (orderItem.getProduct() != null) {
            dto.setProductId(orderItem.getProduct().getId());
            dto.setProductName(orderItem.getProduct().getName());
            dto.setProductPrice(orderItem.getProduct().getPrice());
        }

        return dto;
    }
}
