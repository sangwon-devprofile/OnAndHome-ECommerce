package com.onandhome.inactive_user;

import com.onandhome.cart.CartItemRepository;
import com.onandhome.inactive_user.dto.InactiveUserDTO;
import com.onandhome.inactive_user.entity.InactiveUser;
import com.onandhome.notification.NotificationRepository;
import com.onandhome.order.OrderRepository;
import com.onandhome.review.ReviewRepository;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 탈퇴 회원 Service
 * User → InactiveUser 이동 로직 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InactiveUserService {

    private final InactiveUserRepository inactiveUserRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;

    /**
     * 회원 탈퇴 처리 (User → InactiveUser 이동)
     * 1. User 데이터를 InactiveUser로 복사
     * 2. 연관 데이터 삭제 (알림, 장바구니, 주문, 리뷰)
     * 3. User 테이블에서 삭제
     */
    public InactiveUserDTO moveToInactive(Long userId) {
        // 1. User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + userId));

        log.info("=== 회원 탈퇴 처리 시작 ===");
        log.info("User ID: {}, UserId: {}", user.getId(), user.getUserId());

        // 2. InactiveUser 생성 및 저장
        InactiveUser inactiveUser = InactiveUser.fromUser(user);
        InactiveUser savedInactiveUser = inactiveUserRepository.save(inactiveUser);
        log.info("InactiveUser 저장 완료 - ID: {}", savedInactiveUser.getId());

        // 3. 연관 데이터 삭제
        deleteRelatedData(user);

        // 4. User 테이블에서 삭제
        userRepository.delete(user);
        log.info("User 테이블에서 삭제 완료");

        log.info("=== 회원 탈퇴 처리 완료 ===");

        return InactiveUserDTO.fromEntity(savedInactiveUser);
    }

    /**
     * 여러 회원 탈퇴 처리
     */
    public int moveMultipleToInactive(List<Long> userIds) {
        int successCount = 0;

        for (Long userId : userIds) {
            try {
                moveToInactive(userId);
                successCount++;
            } catch (Exception e) {
                log.error("회원 탈퇴 처리 실패 - userId: {}, error: {}", userId, e.getMessage());
            }
        }

        return successCount;
    }

    /**
     * 연관 데이터 삭제
     */
    private void deleteRelatedData(User user) {
        try {
            // 1. 알림 삭제
            log.info("알림 삭제 시작 - userId: {}", user.getUserId());
            notificationRepository.deleteAll(notificationRepository.findByUserOrderByCreatedAtDesc(user));

            // 2. 장바구니 아이템 삭제
            log.info("장바구니 삭제 시작 - userId: {}", user.getUserId());
            cartItemRepository.deleteByUser(user);

            // 3. 주문 삭제 (주문 아이템은 cascade로 자동 삭제)
            log.info("주문 삭제 시작 - userId: {}", user.getUserId());
            orderRepository.deleteAll(orderRepository.findByUser(user));

            // 4. 리뷰 삭제 (리뷰 답글, 이미지, 좋아요는 cascade로 자동 삭제)
            log.info("리뷰 삭제 시작 - userId: {}", user.getUserId());
            reviewRepository.deleteAll(reviewRepository.findByUser(user));

            log.info("연관 데이터 삭제 완료");
        } catch (Exception e) {
            log.error("연관 데이터 삭제 중 오류: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 모든 탈퇴 회원 조회 (최근 탈퇴 순)
     */
    @Transactional(readOnly = true)
    public List<InactiveUserDTO> getAllInactiveUsers() {
        return inactiveUserRepository.findAllByOrderByDeletedAtDesc().stream()
                .map(InactiveUserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * ID로 탈퇴 회원 조회
     */
    @Transactional(readOnly = true)
    public Optional<InactiveUserDTO> getInactiveUserById(Long id) {
        return inactiveUserRepository.findById(id)
                .map(InactiveUserDTO::fromEntity);
    }

    /**
     * userId로 탈퇴 회원 조회
     */
    @Transactional(readOnly = true)
    public Optional<InactiveUserDTO> getInactiveUserByUserId(String userId) {
        return inactiveUserRepository.findByUserId(userId)
                .map(InactiveUserDTO::fromEntity);
    }

    /**
     * 탈퇴 회원 검색 (이름 또는 userId)
     */
    @Transactional(readOnly = true)
    public List<InactiveUserDTO> searchInactiveUsers(String keyword) {
        return inactiveUserRepository.findAll().stream()
                .filter(user ->
                        (user.getUsername() != null && user.getUsername().contains(keyword)) ||
                        (user.getUserId() != null && user.getUserId().contains(keyword))
                )
                .sorted((u1, u2) -> u2.getDeletedAt().compareTo(u1.getDeletedAt()))
                .map(InactiveUserDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 탈퇴 회원 영구 삭제
     */
    public void permanentDelete(Long id) {
        InactiveUser inactiveUser = inactiveUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 탈퇴 회원입니다. ID: " + id));

        inactiveUserRepository.delete(inactiveUser);
        log.info("탈퇴 회원 영구 삭제 완료 - userId: {}", inactiveUser.getUserId());
    }

    /**
     * 여러 탈퇴 회원 영구 삭제
     */
    public int permanentDeleteMultiple(List<Long> ids) {
        int deletedCount = 0;

        for (Long id : ids) {
            try {
                permanentDelete(id);
                deletedCount++;
            } catch (Exception e) {
                log.error("탈퇴 회원 영구 삭제 실패 - ID: {}, error: {}", id, e.getMessage());
            }
        }

        return deletedCount;
    }

    /**
     * 탈퇴 회원 수 조회
     */
    @Transactional(readOnly = true)
    public long countInactiveUsers() {
        return inactiveUserRepository.count();
    }
}

