package com.onandhome.user;

import com.onandhome.cart.CartItemRepository;
import com.onandhome.notification.NotificationRepository;
import com.onandhome.order.OrderRepository;
import com.onandhome.review.ReviewRepository;
import com.onandhome.user.dto.UserDTO;
import com.onandhome.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입: userId 중복 확인 → 비밀번호 암호화 → 저장
    public UserDTO register(UserDTO userDTO) {
        if (userRepository.existsByUserId(userDTO.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (userDTO.getRole() == null) userDTO.setRole(1);
        if (userDTO.getActive() == null) userDTO.setActive(true);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());
        userDTO.setPassword(encodedPassword);

        User user = userDTO.toEntity();
        User savedUser = userRepository.save(user);

        log.info("새 사용자 등록: {}", userDTO.getUserId());
        return UserDTO.fromEntity(savedUser);
    }

    // 로그인: userId로 조회 → 비활성 여부 확인 → BCrypt 비밀번호 검증
    public Optional<UserDTO> login(String userId, String password) {
        Optional<User> userOptional = userRepository.findByUserId(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (!user.getActive()) {
                log.warn("비활성 사용자: {}", userId);
                return Optional.empty();
            }

            // BCrypt로 입력 비밀번호와 저장된 비밀번호 비교
            if (passwordEncoder.matches(password, user.getPassword())) {
                log.info("사용자 로그인 성공: {}", userId);
                return Optional.of(UserDTO.fromEntity(user));
            } else {
                log.warn("비밀번호 불일치: {}", userId);
            }
        } else {
            log.warn("존재하지 않는 사용자: {}", userId);
        }

        return Optional.empty();
    }

    // userId로 사용자 조회 (DTO 반환)
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .map(UserDTO::fromEntity);
    }

    // ID로 사용자 조회 (DTO 반환)
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDTO::fromEntity);
    }

    /** ✅ ID로 사용자 조회 (Entity) - 관리자 API용 */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /** ✅ 사용자 정보 업데이트 */
    // 사용자 정보 수정: 전달된 필드만 업데이트
    public UserDTO updateUser(UserDTO userDTO) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 전달된 값만 업데이트
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        if (userDTO.getUsername() != null) user.setUsername(userDTO.getUsername());
        if (userDTO.getPhone() != null) user.setPhone(userDTO.getPhone());
        if (userDTO.getGender() != null) user.setGender(userDTO.getGender());
        if (userDTO.getBirthDate() != null) user.setBirthDate(userDTO.getBirthDate());
        if (userDTO.getAddress() != null) user.setAddress(userDTO.getAddress());

        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 업데이트: {}", userDTO.getUserId());
        return UserDTO.fromEntity(updatedUser);
    }

    /** ✅ 회원 정보 저장 (Entity) - 관리자 API용 */
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    /** ✅ 비밀번호 변경 - BCrypt 암호화 */
    // 비밀번호 변경: 기존 비밀번호 확인 → 새 비밀번호 암호화 저장
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 기존 비밀번호 검증
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("비밀번호 변경: {}", user.getUserId());
    }

    // 사용자 삭제: 연관된 데이터(알림, 장바구니, 주문, 리뷰) 먼저 삭제 후 사용자 삭제
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 상태만 비활성화
        user.setActive(false);
        userRepository.save(user);
        log.info("사용자 탈퇴 처리 (Soft Delete): {}", user.getUserId());
    }

    /** ✅ 사용자 영구 삭제 (Hard Delete) */
    public void permanentDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 1. 알림 삭제
        log.info("알림 삭제 - userId: {}", user.getUserId());
        notificationRepository.deleteAll(notificationRepository.findByUserOrderByCreatedAtDesc(user));

        // 2. 장바구니 아이템 삭제
        log.info("장바구니 아이템 삭제 - userId: {}", user.getUserId());
        cartItemRepository.deleteByUser(user);

        // 3. 주문 삭제 (주문 아이템은 cascade로 자동 삭제됨)
        log.info("주문 삭제 - userId: {}", user.getUserId());
        orderRepository.deleteAll(orderRepository.findByUser(user));

        // 4. 리뷰 삭제 (리뷰 답글은 cascade로 자동 삭제됨)
        log.info("리뷰 삭제 - userId: {}", user.getUserId());
        reviewRepository.deleteAll(reviewRepository.findByUser(user));
        
        // 5. 사용자 삭제
        userRepository.delete(user);
        log.info("사용자 삭제 완료: {}", user.getUserId());
    }

    /** ✅ 회원 삭제 (여러 명) - 관리자 API용 (Soft Delete) */
    @Transactional
    public void deleteUsers(List<Long> userIds) {
        for (Long userId : userIds) {
            try {
                deleteUser(userId);
            } catch (Exception e) {
                log.error("회원 삭제 실패 - userId: {}", userId, e);
            }
        }
    }

    /** ✅ 현재 로그인한 사용자 반환 */
    // 현재 로그인한 사용자 조회 (Spring Security authentication 사용)
    @Transactional(readOnly = true)
    public User getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().equals("anonymousUser")) {
            throw new IllegalStateException("로그인된 사용자가 없습니다.");
        }

        String userId = authentication.getName();

        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + userId));
    }

    // 엔티티 그대로 반환 (특정 로직에서 필요)
    @Transactional(readOnly = true)
    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디의 사용자가 존재하지 않습니다: " + userId));
    }

    // 관리자 조회: admin 아이디 우선, 없으면 role=0인 계정 탐색
    @Transactional(readOnly = true)
    public User getAdminUser() {
        return userRepository.findByUserId("admin")
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> u.getRole() != null && u.getRole() == 0)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("관리자 계정을 찾을 수 없습니다.")));
    }

    // 모든 사용자 조회: 최신 가입 순 정렬 후 DTO 변환
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findByActiveTrue().stream()
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .map(UserDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    /** ✅ 탈퇴 사용자 조회 (관리자용) */
    @Transactional(readOnly = true)
    public List<UserDTO> getInactiveUsers() {
        return userRepository.findByActiveFalse().stream()
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .map(UserDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    // username으로 엔티티 반환(컨트롤러에서 사용됨)
    @Transactional(readOnly = true)
    public User getUser(String username) {
        return userRepository.findByUserId(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다: " + username));
    }

    // 이름 또는 아이디로 검색
    @Transactional(readOnly = true)
    public List<UserDTO> search(String keyword) {
        return userRepository.findAll().stream()
                .filter(user ->
                        (user.getUsername() != null && user.getUsername().contains(keyword)) ||
                                (user.getUserId() != null && user.getUserId().contains(keyword))
                )
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .map(UserDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    // 이메일 기반 비밀번호 재설정
    @Transactional
    public boolean resetPasswordByEmail(String email, String newPassword) {
        log.info("비밀번호 재설정 시도: {}", email);
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            log.warn("해당 이메일의 사용자를 찾을 수 없음: {}", email);
            return false;
        }
        
        User user = userOptional.get();
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encryptedPassword);
        userRepository.save(user);
        
        log.info("비밀번호 재설정 성공: {}", email);
        return true;
    }
}