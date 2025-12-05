package com.onandhome.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.onandhome.order.entity.Order;
import com.onandhome.user.entity.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 사용자의 모든 주문 조회
    // 숨김 여부와 관계 없이 전체 주문 리스트를 가져올 때 사용된다.
    // 기본적으로 정렬은 적용되어 있지 않기 때문에 필요 시 서비스단에서 정렬한다.
    List<Order> findByUser(User user);

    // 사용자의 주문을 최신순(createdAt 기준)으로 조회
    // 주문 내역 페이지나 마이페이지에서 최신 주문이 위쪽에 표시되어야 할 때 사용된다.
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    // 사용자의 숨김 처리되지 않은 주문만 조회 (최신순)
    // 마이페이지 기본 주문 목록에서 사용하는 핵심 쿼리.
    // hidden = true 로 설정된 주문은 표시되지 않는다.
    List<Order> findByUserAndHiddenFalseOrderByCreatedAtDesc(User user);

    // 사용자의 모든 주문 조회 (숨김 포함, 최신순)
    // 관리자나 사용자 본인이 전체 기록을 보고 싶을 때 사용.
    // hidden 값으로 정렬하기 때문에 숨김 주문이 뒤쪽에 배치된다.
    List<Order> findByUserOrderByHiddenAscCreatedAtDesc(User user);

    // 주문번호로 주문 조회
    // 외부 결제 연동 시 생성되는 orderNumber 기반의 상세 조회에 사용된다.
    Optional<Order> findByOrderNumber(String orderNumber);

    // 오늘 주문 개수 조회
    // createdAt이 오늘 날짜(startOfDay 이상)인 주문 수를 계산하여
    // 관리자 대시보드의 "오늘 주문 수" 통계에 활용된다.
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay")
    long countTodayOrders(@Param("startOfDay") LocalDateTime startOfDay);

    // 오늘 매출 합계 조회
    // totalPrice는 실제 결제 완료된 금액을 나타낸다.
    // 취소(CANCELED) 상태의 주문은 매출에서 제외하기 위해 조건을 추가했다.
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.createdAt >= :startOfDay AND o.status != :canceledStatus")
    long sumTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay,
                         @Param("canceledStatus") Order.OrderStatus canceledStatus);

    // 이달 매출 합계 조회
    // startOfMonth(예: 2025-03-01 00:00)를 기준으로 그 달의 매출을 계산한다.
    // 월별 매출 그래프나 관리자 매출 통계 기능에서 사용된다.
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.createdAt >= :startOfMonth AND o.status != :canceledStatus")
    long sumMonthRevenue(@Param("startOfMonth") LocalDateTime startOfMonth,
                         @Param("canceledStatus") Order.OrderStatus canceledStatus);
}
