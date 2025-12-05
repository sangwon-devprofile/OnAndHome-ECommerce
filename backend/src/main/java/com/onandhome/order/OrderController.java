package com.onandhome.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onandhome.order.dto.CreateOrderRequest;
import com.onandhome.order.dto.OrderDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성 API
     * 주문 생성 시점에서는 결제 여부와 상관없이 Order 객체가 DB에 생성된다.
     * 결제 방법이 카드인 경우에는 생성 직후 상태가 ORDERED가 되고,
     * 무통장 입금인 경우에는 PAYMENT_PENDING 상태로 저장된다.
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateOrderRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== 주문 생성 API 호출 ===");
            log.info("User ID: {}", request.getUserId());
            log.info("Payment Method: {}", request.getPaymentMethod());
            log.info("Order Items: {}", request.getOrderItems());

            OrderDTO orderDTO = orderService.createOrder(request);

            /* 주문 생성 완료 후 반환되는 DTO에는
               주문 상태(ORDERED 또는 PAYMENT_PENDING),
               총 금액, 주문 번호 등 결제 전 기본 정보가 포함된다. */
            log.info("주문 생성 성공: orderId={}, status={}", orderDTO.getOrderId(), orderDTO.getStatus());

            response.put("success", true);
            response.put("message", "주문이 생성되었습니다.");
            response.put("data", orderDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("주문 생성 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("주문 생성 중 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "주문 생성 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 사용자의 모든 주문 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOrders(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("사용자 주문 목록 조회: userId={}", userId);

            List<OrderDTO> orders = orderService.getOrders(userId);

            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("주문 조회 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("주문 조회 중 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "주문 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("주문 상세 조회: orderId={}", orderId);

            OrderDTO order = orderService.getOrder(orderId);
            response.put("success", true);
            response.put("order", order);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("주문 조회 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "주문 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 주문 결제 처리
     * 주문 생성 후 실제 결제가 완료될 때 호출되는 API.
     * pay() 메서드 내에서 상태가 ORDERED로 변경되고 결제 시간이 기록된다.
     * 결제가 완료되면 알림 서비스나 WebSocket 푸시 등이 내부적으로 실행된다.
     */
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Map<String, Object>> pay(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("주문 결제 처리: orderId={}", orderId);

            OrderDTO orderDTO = orderService.pay(orderId);

            /* 반환되는 orderDTO는 결제 완료 상태(ORDERED)로 업데이트되어 있으며,
               paidAt 필드가 결제 시각으로 채워져 있다. */
            response.put("success", true);
            response.put("message", "결제가 완료되었습니다.");
            response.put("data", orderDTO);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("결제 중 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "결제 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 주문 취소
     * 배송완료 이전 상태에서만 취소가 가능하며,
     * 취소 시 OrderItem 내부에서 재고 복구 로직이 실행된다.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("주문 취소 요청: orderId={}", orderId);

            OrderDTO orderDTO = orderService.cancel(orderId);
            response.put("success", true);
            response.put("message", "주문이 취소되었습니다.");
            response.put("data", orderDTO);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("주문 취소 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "주문 취소 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 주문 배송 상태 조회
     */
    @GetMapping("/{orderId}/track")
    public ResponseEntity<Map<String, Object>> track(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("배송 상태 조회: orderId={}", orderId);

            String trackingStatus = orderService.track(orderId);
            response.put("success", true);
            response.put("data", trackingStatus);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("배송 조회 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "배송 상태 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 장바구니 기반 주문 생성
     * 장바구니의 모든 상품을 OrderItem으로 생성하고 주문 처리 흐름은 일반 주문과 동일하다.
     */
    @PostMapping("/cart/create")
    public ResponseEntity<Map<String, Object>> createFromCart(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("장바구니 주문 생성: userId={}", userId);

            OrderDTO orderDTO = orderService.createOrderFromCart(userId);
            response.put("success", true);
            response.put("message", "주문이 생성되었습니다.");
            response.put("data", orderDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("장바구니 주문 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "주문 생성 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 주문 숨김 처리
     * DB에는 남아있지만 사용자 마이페이지에서는 표시되지 않도록 한다.
     */
    @PostMapping("/{orderId}/hide")
    public ResponseEntity<Map<String, Object>> hideOrder(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("주문 숨김 요청: orderId={}", orderId);

            orderService.hideOrder(orderId);
            response.put("success", true);
            response.put("message", "주문이 숨김 처리되었습니다.");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("주문 숨김 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "주문 숨김 처리 중 문제가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 주문 숨김 해제
     */
    @PostMapping("/{orderId}/unhide")
    public ResponseEntity<Map<String, Object>> unhideOrder(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("주문 숨김 해제 요청: orderId={}", orderId);

            orderService.unhideOrder(orderId);
            response.put("success", true);
            response.put("message", "주문이 다시 표시됩니다.");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("주문 숨김 해제 오류: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "주문 숨김 해제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
