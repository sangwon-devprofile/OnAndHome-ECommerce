package com.onandhome.admin.controller;

import com.onandhome.Notice.NoticeService;
import com.onandhome.admin.adminProduct.ProductRepository;
import com.onandhome.order.OrderRepository;
import com.onandhome.order.OrderService;
import com.onandhome.order.dto.OrderDTO;
import com.onandhome.qna.QnaRepository;
import com.onandhome.review.ReviewRepository;
import com.onandhome.user.UserRepository;
import com.onandhome.user.UserService;
import com.onandhome.user.dto.UserDTO;
import com.onandhome.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관리자 공통 페이지 컨트롤러
 * 사용자 관리, 주문 관리, 대시보드 화면을 담당
 * 상품 관리는 AdminProductController에서 처리됨
 */
@Log4j2
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
public class AdminController {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final NoticeService noticeService;
    private final ReviewRepository reviewRepository;
    private final QnaRepository qnaRepository;

    // ==================== 사용자 관리 (Thymeleaf 뷰) ====================

    @GetMapping("/user/list")
    public String userList(@RequestParam(value = "kw", required = false) String keyword, Model model) {
        try {
            List<UserDTO> users;
            // 검색어가 있으면 검색 결과만
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userService.search(keyword);
                model.addAttribute("kw", keyword);
            } else {
                users = userService.getAllUsers();
            }
            model.addAttribute("users", users);

        } catch (Exception e) {
            // 오류 시 빈 리스트 전달
            model.addAttribute("users", List.of());
        }

        return "admin/user/list";
    }

    // 사용자 상세 페이지
    @GetMapping("/user/detail")
    public String userDetail(@RequestParam("id") Long userId, Model model) {
        try {
            // id로 회원 조회
            UserDTO user = userService.getUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            model.addAttribute("user", user);

        } catch (Exception e) {
            model.addAttribute("error", "사용자 정보를 불러올 수 없습니다.");
        }

        return "admin/user/detail";
    }

    // 단일 사용자 삭제 (AJAX 요청)
    @PostMapping("/user/delete")
    @ResponseBody
    public Map<String, Object> deleteUser(@RequestParam("id") Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.deleteUser(userId);
            response.put("success", true);
            response.put("message", "회원이 삭제되었습니다.");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "회원 삭제에 실패했습니다: " + e.getMessage());
        }

        return response;
    }

    // 다중 사용자 삭제 (AJAX 요청)
    @PostMapping("/user/delete-multiple")
    @ResponseBody
    public Map<String, Object> deleteMultipleUsers(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Long> userIds = request.get("ids");

            if (userIds == null || userIds.isEmpty()) {
                response.put("success", false);
                response.put("message", "삭제할 회원을 선택해주세요.");
                return response;
            }

            int deletedCount = 0;

            // 선택된 사용자들 반복 삭제
            for (Long userId : userIds) {
                try {
                    userService.deleteUser(userId);
                    deletedCount++;
                } catch (Exception e) {
                    log.error("회원 ID {} 삭제 실패", userId, e);
                }
            }

            response.put("success", true);
            response.put("message", deletedCount + "명의 회원이 삭제되었습니다.");
            response.put("deletedCount", deletedCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "회원 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return response;
    }

    // ==================== React 회원 관리 API ====================

    @GetMapping("/api/admin/users")
    @ResponseBody
    public ResponseEntity<?> getAllUsersAPI(@RequestParam(required = false) String kw) {
        log.info("=== 관리자 회원 목록 조회 API 호출 ===");
        log.info("검색어: {}", kw);
        try {
            List<UserDTO> users;
            if (kw != null && !kw.trim().isEmpty()) {
                users = userService.search(kw.trim());
            } else {
                users = userService.getAllUsers();
            }
            log.info("전체 회원 수: {}명", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("회원 목록 조회 실패", e);
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    @GetMapping("/api/admin/users/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserDetailAPI(@PathVariable Long userId) {
        log.info("========================================");
        log.info("회원 상세 정보 조회 API 호출됨!");
        log.info("userId: {}", userId);
        log.info("========================================");

        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다. ID: " + userId));

            log.info("조회된 회원 정보:");
            log.info("  ID: {}", user.getId());
            log.info("  UserId: {}", user.getUserId());
            log.info("  Username: {}", user.getUsername());
            log.info("  Email: {}", user.getEmail());
            log.info("  Phone: {}", user.getPhone());
            log.info("  Gender: {}", user.getGender());
            log.info("  BirthDate: {}", user.getBirthDate());
            log.info("  Address: {}", user.getAddress());

            // ⭐ success를 true로 설정하고 나머지 필드는 최상위 레벨에 배치
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", user.getId());
            response.put("userId", user.getUserId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("phone", user.getPhone());
            response.put("gender", user.getGender());
            response.put("birthDate", user.getBirthDate());
            response.put("address", user.getAddress());
            response.put("createdAt", user.getCreatedAt());

            log.info("응답 전송 완료");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("회원 정보 조회 실패 - userId: {}", userId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(404).body(errorResponse);
        }
    }

    @PostMapping("/api/admin/users/{userId}/update")
    @ResponseBody
    public ResponseEntity<?> updateUserAPI(
            @PathVariable Long userId,
            @RequestBody Map<String, String> updateData) {

        log.info("========================================");
        log.info("회원 정보 수정 API 호출됨!");
        log.info("userId: {}", userId);
        log.info("updateData: {}", updateData);
        log.info("========================================");

        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

            log.info("기존 회원 정보: {}", user);

            if (updateData.containsKey("username") && updateData.get("username") != null) {
                user.setUsername(updateData.get("username"));
            }
            if (updateData.containsKey("email") && updateData.get("email") != null) {
                user.setEmail(updateData.get("email"));
            }
            if (updateData.containsKey("phone") && updateData.get("phone") != null) {
                user.setPhone(updateData.get("phone"));
            }
            if (updateData.containsKey("gender") && updateData.get("gender") != null) {
                user.setGender(updateData.get("gender"));
            }
            if (updateData.containsKey("birthDate") && updateData.get("birthDate") != null) {
                user.setBirthDate(updateData.get("birthDate"));
            }
            if (updateData.containsKey("address") && updateData.get("address") != null) {
                user.setAddress(updateData.get("address"));
            }

            log.info("수정할 회원 정보: {}", user);

            User savedUser = userService.save(user);

            log.info("저장된 회원 정보: {}", savedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "회원 정보가 수정되었습니다.");
            response.put("data", savedUser);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("회원 정보 수정 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/api/admin/users/delete")
    @ResponseBody
    public ResponseEntity<?> deleteUsersAPI(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "삭제할 회원을 선택해주세요.")
                );
            }

            int deletedCount = 0;
            for (Long userId : ids) {
                try {
                    userService.deleteUser(userId);
                    deletedCount++;
                } catch (Exception e) {
                    log.error("회원 ID {} 삭제 실패", userId, e);
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", deletedCount + "명의 회원이 삭제되었습니다.",
                    "deletedCount", deletedCount
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ==================== 상품 관리 ====================

    @GetMapping("/product/detail")
    public String productDetail() {
        return "admin/product/detail";
    }

    // ==================== 주문 관리 ====================

    // 주문 목록 페이지
    @GetMapping("/order/list")
    public String orderList(@RequestParam(value = "kw", required = false) String keyword, Model model) {
        try {
            List<OrderDTO> orders;

            // 검색어 있으면 검색 결과
            if (keyword != null && !keyword.trim().isEmpty()) {
                orders = orderService.search(keyword);
                model.addAttribute("kw", keyword);
            }
            // 없으면 전체 주문 목록
            else {
                orders = orderService.getAllOrders();
            }

            model.addAttribute("orders", orders);

        } catch (Exception e) {
            // 오류 시 빈 리스트 전달
            model.addAttribute("orders", List.of());
        }

        return "admin/order/list";
    }

    // 주문 상세 페이지
    @GetMapping("/order/detail")
    public String orderDetail(@RequestParam("id") Long orderId, Model model) {
        try {
            // 주문 정보 조회
            OrderDTO order = orderService.getOrder(orderId);
            model.addAttribute("order", order);
        } catch (Exception e) {
            model.addAttribute("error", "주문 정보를 불러올 수 없습니다.");
        }
        return "admin/order/detail";
    }

    // ==================== 게시판 대시보드 ====================

    @GetMapping("/board/dashboard")
    public String boardDashboard(Model model) {
        try {
            long totalNotices = noticeService.findAll().size();
            model.addAttribute("totalNotices", totalNotices);

            long totalReviews = reviewRepository.count();
            model.addAttribute("totalReviews", totalReviews);

            long totalQnas = qnaRepository.count();
            model.addAttribute("totalQnas", totalQnas);

        } catch (Exception e) {
            model.addAttribute("totalNotices", 0L);
            model.addAttribute("totalReviews", 0L);
            model.addAttribute("totalQnas", 0L);
        }
        
        return "admin/board/dashboard";
    }

    // ==================== 대시보드 ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();

            // 주문/매출 통계
            long todayOrderCount = orderRepository.countTodayOrders(startOfDay);
            long todayRevenue = orderRepository.sumTodayRevenue(startOfDay, com.onandhome.order.entity.Order.OrderStatus.CANCELED);
            long monthRevenue = orderRepository.sumMonthRevenue(startOfMonth, com.onandhome.order.entity.Order.OrderStatus.CANCELED);

            model.addAttribute("todayOrderCount", todayOrderCount);
            model.addAttribute("todayRevenue", todayRevenue);
            model.addAttribute("monthRevenue", monthRevenue);

            long totalProducts = productRepository.count();
            long outOfStockProducts = productRepository.countOutOfStockProducts();

            model.addAttribute("totalProducts", totalProducts);
            model.addAttribute("outOfStockProducts", outOfStockProducts);

            long todayNewUsers = userRepository.countTodayNewUsers(startOfDay);
            long totalUsers = userRepository.count();
            long inactiveUsers = userRepository.countInactiveUsers();

            model.addAttribute("todayNewUsers", todayNewUsers);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("inactiveUsers", inactiveUsers);

            long totalNotices = noticeService.findAll().size();
            long totalReviews = reviewRepository.count();
            long totalQnas = qnaRepository.count();
            // 게시판 통계
            model.addAttribute("totalNotices", totalNotices);
            model.addAttribute("totalReviews", totalReviews);
            model.addAttribute("totalQnas", totalQnas);

        } catch (Exception e) {
            // 오류 발생 시 전체 항목 기본값 처리
            model.addAttribute("todayOrderCount", 0L);
            model.addAttribute("todayRevenue", 0L);
            model.addAttribute("monthRevenue", 0L);
            model.addAttribute("totalProducts", 0L);
            model.addAttribute("outOfStockProducts", 0L);
            model.addAttribute("todayNewUsers", 0L);
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("inactiveUsers", 0L);
            model.addAttribute("totalNotices", 0L);
            model.addAttribute("totalReviews", 0L);
            model.addAttribute("totalQnas", 0L);
        }

        log.info("=== 대시보드 데이터 조회 ===");
        log.info("오늘 주문: {}", model.getAttribute("todayOrderCount"));
        log.info("오늘 매출: {}", model.getAttribute("todayRevenue"));
        log.info("이달 매출: {}", model.getAttribute("monthRevenue"));
        log.info("전체 상품: {}", model.getAttribute("totalProducts"));
        log.info("품절 상품: {}", model.getAttribute("outOfStockProducts"));
        log.info("오늘 신규 회원: {}", model.getAttribute("todayNewUsers"));
        log.info("전체 회원: {}", model.getAttribute("totalUsers"));
        log.info("탈퇴 회원: {}", model.getAttribute("inactiveUsers"));

        return "admin/dashboard";
    }
}