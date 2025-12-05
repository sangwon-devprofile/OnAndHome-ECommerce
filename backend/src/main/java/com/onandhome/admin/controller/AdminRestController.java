package com.onandhome.admin.controller;

import com.onandhome.Notice.NoticeService;
import com.onandhome.admin.adminProduct.ProductRepository;
import com.onandhome.inactive_user.InactiveUserRepository;
import com.onandhome.inactive_user.InactiveUserService;
import com.onandhome.inactive_user.dto.InactiveUserDTO;
import com.onandhome.order.OrderRepository;
import com.onandhome.qna.QnaRepository;
import com.onandhome.review.ReviewRepository;
import com.onandhome.user.UserRepository;
import com.onandhome.user.UserService;
import com.onandhome.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 REST API 컨트롤러
 * JSON 형태로 관리자 페이지에서 사용하는 데이터를 반환한다.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRestController {

    private final UserService userService;
    private final InactiveUserService inactiveUserService;
    private final InactiveUserRepository inactiveUserRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final NoticeService noticeService;
    private final ReviewRepository reviewRepository;
    private final QnaRepository qnaRepository;

    /**
     * 관리자 대시보드 데이터 조회
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> response = new HashMap<>();
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
            
            // 매출 현황
            long todayOrderCount = orderRepository.countTodayOrders(startOfDay);
            long todayRevenue = orderRepository.sumTodayRevenue(startOfDay, com.onandhome.order.entity.Order.OrderStatus.CANCELED);
            long monthRevenue = orderRepository.sumMonthRevenue(startOfMonth, com.onandhome.order.entity.Order.OrderStatus.CANCELED);
            
            response.put("todayOrderCount", todayOrderCount);
            response.put("todayRevenue", todayRevenue);
            response.put("monthRevenue", monthRevenue);

            // 상품 현황
            long totalProducts = productRepository.count();
            long outOfStockProducts = productRepository.countOutOfStockProducts();
            
            response.put("totalProducts", totalProducts);
            response.put("outOfStockProducts", outOfStockProducts);

            // 회원 현황
            long todayNewUsers = userRepository.countTodayNewUsers(startOfDay);
            long totalUsers = userRepository.count();
            long inactiveUsers = inactiveUserRepository.count(); // inactive_user 테이블에서 조회
            
            response.put("todayNewUsers", todayNewUsers);
            response.put("totalUsers", totalUsers);
            response.put("inactiveUsers", inactiveUsers);

            // 게시판 현황
            long totalNotices = noticeService.findAll().size();
            long totalReviews = reviewRepository.count();
            long totalQnas = qnaRepository.count();

            response.put("totalNotices", totalNotices);
            response.put("totalReviews", totalReviews);
            response.put("totalQnas", totalQnas);

            System.out.println("=== 대시보드 데이터 조회 ===");
            System.out.println("오늘 주문: " + todayOrderCount);
            System.out.println("오늘 매출: " + todayRevenue);
            System.out.println("이달 매출: " + monthRevenue);
            System.out.println("전체 상품: " + totalProducts);
            System.out.println("품절 상품: " + outOfStockProducts);
            System.out.println("오늘 신규 회원: " + todayNewUsers);
            System.out.println("전체 회원: " + totalUsers);
            System.out.println("탈퇴 회원: " + inactiveUsers);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("대시보드 데이터 조회 오류: " + e.getMessage());
            e.printStackTrace();
            
            // 오류 발생 시 기본값 반환
            response.put("todayOrderCount", 0L);
            response.put("todayRevenue", 0L);
            response.put("monthRevenue", 0L);
            response.put("totalProducts", 0L);
            response.put("outOfStockProducts", 0L);
            response.put("todayNewUsers", 0L);
            response.put("totalUsers", 0L);
            response.put("inactiveUsers", 0L);
            response.put("totalNotices", 0L);
            response.put("totalReviews", 0L);
            response.put("totalQnas", 0L);
            
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 관리자 테스트 API
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "관리자 API 정상 작동");
        response.put("timestamp", System.currentTimeMillis());
        System.out.println("테스트 API 호출됨");
        return ResponseEntity.ok(response);
    }

    /**
     * 탈퇴 회원 목록 조회 API (inactive 경로)
     */
    @GetMapping("/users/inactive")
    public ResponseEntity<List<InactiveUserDTO>> getInactiveUserList() {
        try {
            List<InactiveUserDTO> users = inactiveUserService.getAllInactiveUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * 탈퇴 회원 목록 조회 API (deleted 경로 - 프론트엔드 호환용)
     * 검색 키워드 지원
     */
    @GetMapping("/users/deleted")
    public ResponseEntity<List<InactiveUserDTO>> getDeletedUserList(
            @RequestParam(value = "kw", required = false) String keyword) {
        try {
            System.out.println("=== 탈퇴 회원 목록 조회 API 호출 ===");
            System.out.println("검색어: " + keyword);
            
            List<InactiveUserDTO> users;
            
            // 검색 키워드가 있는 경우 필터링
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = inactiveUserService.searchInactiveUsers(keyword.trim());
            } else {
                users = inactiveUserService.getAllInactiveUsers();
            }
            
            System.out.println("탈퇴 회원 수: " + users.size() + "명");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("탈퇴 회원 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * 관리자 회원 목록 조회 API
     * 회원 검색 또는 전체 조회
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUserList(@RequestParam(value = "kw", required = false) String keyword) {
        try {
            System.out.println("=== 관리자 회원 목록 조회 API 호출 ===");
            System.out.println("검색어: " + keyword);
            
            List<UserDTO> users;

            // 검색 키워드가 있는 경우: 키워드로 회원 검색
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userService.search(keyword);
            }
            // 키워드 없으면 전체 회원 조회
            else {
                users = userService.getAllUsers();
                System.out.println("전체 회원 수: " + users.size() + "명");
            }
            
            // 회원 정보 출력 (테스트용)
            if (users.size() > 0) {
                System.out.println("첫 번째 회원 정보:");
                UserDTO firstUser = users.get(0);
                System.out.println("  ID: " + firstUser.getId());
                System.out.println("  UserId: " + firstUser.getUserId());
                System.out.println("  Username: " + firstUser.getUsername());
                System.out.println("  Email: " + firstUser.getEmail());
                System.out.println("  Phone: " + firstUser.getPhone());
                System.out.println("  Gender: " + firstUser.getGender());
                System.out.println("  CreatedAt: " + firstUser.getCreatedAt());
            }
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("회원 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();
            // 오류 발생 시 빈 리스트 반환
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * 회원 상세 조회 API
     * 선택된 회원의 상세 정보를 반환
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserDetail(@PathVariable Long id) {
        try {
            // id로 회원 조회, 없으면 예외 발생
            UserDTO user = userService.getUserById(id)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // 회원 찾기 실패 시 404 반환
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 회원 단일 삭제 API (User → InactiveUser 이동)
     * 한 명의 회원만 삭제할 때 사용
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            // User → InactiveUser 이동 처리
            inactiveUserService.moveToInactive(id);
            response.put("success", true);
            response.put("message", "회원이 탈퇴 처리되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "회원 삭제에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 회원 다중 삭제 API (User → InactiveUser 이동)
     * 여러 명을 선택하여 삭제하는 기능
     */
    @PostMapping("/users/delete")
    public ResponseEntity<Map<String, Object>> deleteMultipleUsers(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 요청으로 전달된 회원 ID 목록
            List<Long> userIds = request.get("ids");

            // 아무 것도 선택하지 않은 경우
            if (userIds == null || userIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "삭제할 회원을 선택해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // User → InactiveUser 이동 처리
            int deletedCount = inactiveUserService.moveMultipleToInactive(userIds);
            
            response.put("success", true);
            response.put("message", deletedCount + "명의 회원이 탈퇴 처리되었습니다.");
            response.put("deletedCount", deletedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "회원 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 다중 회원 영구 삭제 API
     */
    @PostMapping("/users/permanent-delete")
    public ResponseEntity<Map<String, Object>> permanentDeleteMultipleUsers(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> userIds = request.get("ids");
            if (userIds == null || userIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "삭제할 회원을 선택해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            int deletedCount = 0;
            for (Long userId : userIds) {
                try {
                    userService.permanentDeleteUser(userId);
                    deletedCount++;
                } catch (Exception e) {
                    System.err.println("회원 ID " + userId + " 영구 삭제 실패: " + e.getMessage());
                }
            }

            response.put("success", true);
            response.put("message", deletedCount + "명의 회원이 영구 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류 발생: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
