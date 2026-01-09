package com.onandhome.qna;

import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.admin.adminProduct.ProductRepository;
import com.onandhome.file.FileStorageService;
import com.onandhome.qna.dto.QnaDTO;
import com.onandhome.qna.dto.QnaImageDTO;
import com.onandhome.qna.dto.QnaReplyDTO;
import com.onandhome.qna.entity.Qna;
import com.onandhome.qna.entity.QnaImage;
import com.onandhome.qna.entity.QnaReply;
import com.onandhome.user.dto.UserDTO;
import com.onandhome.user.entity.User;
import com.onandhome.user.UserRepository;
import com.onandhome.util.JWTUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * QnA REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
@Slf4j
public class QnaRestController {

    private final QnaService qnaService;
    private final ProductRepository productRepository;
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private static final String SESSION_USER_KEY = "loginUser";

    private final FileStorageService fileStorageService;
    private final QnaImageRepository qnaImageRepository;

    /**
     * 특정 상품의 QnA 목록 조회
     * GET /api/qna/product/{productId}
     */
    @Transactional(readOnly = true)
    @GetMapping("/product/{productId}")
    // /product/{productId} 주소를 @RequestMapping("/api/qna")을 통해서 /api/qna/product/{productId} 을 반환한다.
    public ResponseEntity<Map<String, Object>> getQnaByProduct(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("상품 QnA 조회 요청 - productId: {}", productId);

            // 상품별 QnA 조회
            List<Qna> qnaList = qnaRepository.findByProductIdOrderByCreatedAtDesc(productId);

            // DTO 변환 (답글 포함)
            List<QnaDTO> qnaDTOList = qnaList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", qnaDTOList);
            log.info("QnA 조회 성공 - 개수: {}", qnaDTOList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("QnA 조회 오류", e);
            response.put("success", false);
            response.put("message", "QnA를 불러올 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 개별 QnA 조회
     * GET /api/qna/{id}
     */
    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getQnaById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("QnA 상세 조회 요청 - id: {}", id);

            Qna qna = qnaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));

            QnaDTO qnaDTO = convertToDTO(qna);

            response.put("success", true);
            response.put("data", qnaDTO);
            log.info("QnA 상세 조회 성공");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("QnA를 찾을 수 없음", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("QnA 상세 조회 오류", e);
            response.put("success", false);
            response.put("message", "QnA를 불러올 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 최근 QnA 목록 조회 (Footer용)
     * GET /api/qna/recent?limit=8
     */
    @Transactional(readOnly = true)
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentQnas(
            @RequestParam(defaultValue = "8") int limit) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("최근 QnA 목록 조회 요청 - limit: {}", limit);

            List<Qna> qnaList = qnaRepository.findTop100ByOrderByCreatedAtDesc();

            // DTO 변환
            List<QnaDTO> qnaDTOList = qnaList.stream()
                    .limit(limit)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", qnaDTOList);
            log.info("최근 QnA 조회 성공 - 개수: {}", qnaDTOList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("최근 QnA 조회 오류", e);
            response.put("success", false);
            response.put("message", "QnA를 불러올 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 내 QnA 목록 조회 (마이페이지용)
     * GET /api/qna/my
     */
    @Transactional(readOnly = true)
    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyQnas(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("내 QnA 목록 조회 요청");

            String writer = getCurrentUserName(authHeader, session);
            if (writer == null) {
                log.error("인증 정보 없음");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            log.info("조회할 작성자: {}", writer);

            // 작성자로 QnA 조회
            List<Qna> qnaList = qnaRepository.findByWriterOrderByCreatedAtDesc(writer);

            // username으로도 조회 (기존 데이터 호환성)
            try {
                User user = userRepository.findByUserId(writer).orElse(null);
                if (user != null && user.getUsername() != null && !user.getUsername().equals(writer)) {
                    List<Qna> qnaListByUsername = qnaRepository.findByWriterOrderByCreatedAtDesc(user.getUsername());
                    // 중복 제거
                    java.util.Set<Long> existingIds = qnaList.stream().map(Qna::getId).collect(Collectors.toSet());
                    qnaListByUsername.stream()
                            .filter(q -> !existingIds.contains(q.getId()))
                            .forEach(qnaList::add);
                }
            } catch (Exception e) {
                log.error("username으로 조회 실패", e);
            }

            // DTO 변환
            List<QnaDTO> qnaDTOList = qnaList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", qnaDTOList);
            log.info("내 QnA 조회 성공 - 개수: {}", qnaDTOList.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("내 QnA 조회 오류", e);
            response.put("success", false);
            response.put("message", "QnA를 불러올 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * QnA 등록
     * POST /api/qna
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createQna(
            @RequestBody QnaDTO qnaDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== QnA 등록 요청 ===");
            log.info("qnaDTO: {}", qnaDTO);
            log.info("authHeader: {}", authHeader);

            // 작성자 확인 (userId로 통일)
            String writer = getCurrentUserName(authHeader, session);
            log.info("작성자: {}", writer);

            if (writer == null) {
                log.error("인증 정보가 없음");
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            // 상품 조회
            Product product = productRepository.findById(qnaDTO.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

            // Qna 엔티티 생성
            Qna qna = new Qna();
            qna.setTitle(qnaDTO.getTitle() != null ? qnaDTO.getTitle() : "상품 문의");
            qna.setQuestion(qnaDTO.getQuestion());
            qna.setWriter(writer);
            qna.setProduct(product);
            qna.setIsPrivate(qnaDTO.getIsPrivate() != null ? qnaDTO.getIsPrivate() : false);

            // 저장
            Qna savedQna = qnaService.save(qna);

            response.put("success", true);
            response.put("message", "QnA가 등록되었습니다.");
            response.put("data", convertToDTO(savedQna));
            log.info("QnA 등록 성공 - id: {}", savedQna.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("QnA 등록 오류", e);
            response.put("success", false);
            response.put("message", "QnA 등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * QnA 수정
     * PUT /api/qna/{id}
     */
    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateQna(
            @PathVariable Long id,
            @RequestBody QnaDTO qnaDTO,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== QnA 수정 요청 ===");
            log.info("id: {}", id);
            log.info("qnaDTO: {}", qnaDTO);
            log.info("authHeader: {}", authHeader);

            // 기존 QnA 조회
            Qna qna = qnaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));

            log.info("기존 QnA writer: {}", qna.getWriter());

            // 작성자 확인
            if (!isAuthor(qna.getWriter(), authHeader, session)) {
                log.error("작성자 불일치");
                response.put("success", false);
                response.put("message", "작성자만 수정할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }

            // 수정
            qna.setTitle(qnaDTO.getTitle());
            qna.setQuestion(qnaDTO.getQuestion());
            qna.setIsPrivate(qnaDTO.getIsPrivate() != null ? qnaDTO.getIsPrivate() : false);

            Qna updatedQna = qnaRepository.save(qna);

            response.put("success", true);
            response.put("message", "QnA가 수정되었습니다.");
            response.put("data", convertToDTO(updatedQna));
            log.info("QnA 수정 성공 - id: {}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("QnA 수정 실패", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("QnA 수정 오류", e);
            response.put("success", false);
            response.put("message", "QnA 수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * QnA 삭제
     * DELETE /api/qna/{id}
     */
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteQna(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("=== QnA 삭제 요청 ===");
            log.info("id: {}", id);
            log.info("authHeader: {}", authHeader);

            // 기존 QnA 조회
            Qna qna = qnaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("QnA를 찾을 수 없습니다."));

            log.info("기존 QnA writer: {}", qna.getWriter());

            // 작성자 확인
            if (!isAuthor(qna.getWriter(), authHeader, session)) {
                log.error("작성자 불일치");
                response.put("success", false);
                response.put("message", "작성자만 삭제할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }

            // 삭제
            qnaService.delete(id);

            response.put("success", true);
            response.put("message", "QnA가 삭제되었습니다.");
            log.info("QnA 삭제 성공 - id: {}", id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("QnA 삭제 실패", e);
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            log.error("QnA 삭제 오류", e);
            response.put("success", false);
            response.put("message", "QnA 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping(
            value = "/with-images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Transactional
    public ResponseEntity<Map<String, Object>> createQnaWithImages(
            @RequestParam Long productId,
            @RequestParam String question,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpSession session
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("QnA + 이미지 등록 요청: productId={}, title={}, images={}",
                    productId, title, (images == null ? 0 : images.size()));

            // 1) 작성자 확인
            String writer = getCurrentUserName(authHeader, session);
            if (writer == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            // 2) 상품 조회
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

            // 3) QnA 생성 후 저장
            Qna qna = new Qna();
            qna.setProduct(product);
            qna.setQuestion(question);
            qna.setWriter(writer);
            qna.setTitle(title != null ? title : "상품 문의");

            Qna savedQna = qnaService.save(qna);

            // 4) 이미지 저장
            if (images != null && !images.isEmpty()) {
                for (MultipartFile file : images) {
                    if (file == null || file.isEmpty()) continue;

                    String imageUrl = fileStorageService.storeFile(file);

                    QnaImage qnaImage = new QnaImage();
                    qnaImage.setQna(savedQna);
                    qnaImage.setImageUrl(imageUrl);

                    qnaImageRepository.save(qnaImage);
                }
            }

            response.put("success", true);
            response.put("message", "QnA가 등록되었습니다.");
            response.put("data", convertToDTO(savedQna));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("QnA + 이미지 등록 오류", e);
            response.put("success", false);
            response.put("message", "QnA 등록 중 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    /**
     * JWT 토큰 또는 세션에서 사용자 ID 추출 (userId로 통일)
     */
    private String getCurrentUserName(String authHeader, HttpSession session) {
        // 1. JWT 토큰에서 확인
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                log.info("JWT 토큰 확인: {}", token.substring(0, Math.min(20, token.length())) + "...");
                Map<String, Object> claims = jwtUtil.validateToken(token);
                String userId = claims.get("userId").toString();
                log.info("JWT에서 userId 추출: {}", userId);
                return userId;
            } catch (Exception e) {
                log.error("JWT 토큰 검증 실패", e);
            }
        }

        // 2. 세션에서 확인 (userId로 통일)
        UserDTO loginUser = (UserDTO) session.getAttribute(SESSION_USER_KEY);
        if (loginUser != null) {
            String userId = loginUser.getUserId();
            log.info("세션에서 userId 추출: {}", userId);
            return userId;
        }

        log.warn("인증 정보를 찾을 수 없음");
        return null;
    }

    /**
     * 작성자 확인 (기존 writer와 현재 사용자의 userId 또는 username 비교)
     * @param qnaWriter QnA의 writer (기존 데이터는 username일 수 있고, 새 데이터는 userId)
     * @param authHeader JWT 토큰
     * @param session 세션
     * @return 작성자가 맞으면 true
     */
    private boolean isAuthor(String qnaWriter, String authHeader, HttpSession session) {
        if (qnaWriter == null) {
            log.warn("QnA writer가 null");
            return false;
        }

        // 현재 사용자의 userId 추출
        String currentUserId = getCurrentUserName(authHeader, session);
        if (currentUserId == null) {
            log.warn("현재 사용자 정보가 없음");
            return false;
        }

        // 현재 사용자의 username 추출 (DB에서 직접 조회)
        String currentUsername = null;
        try {
            User user = userRepository.findByUserId(currentUserId).orElse(null);
            if (user != null) {
                currentUsername = user.getUsername();
                log.info("DB에서 username 조회: {}", currentUsername);
            }
        } catch (Exception e) {
            log.error("User 조회 실패", e);
        }

        // 세션에서도 확인 (백업)
        if (currentUsername == null) {
            UserDTO loginUser = (UserDTO) session.getAttribute(SESSION_USER_KEY);
            if (loginUser != null) {
                currentUsername = loginUser.getUsername();
                log.info("세션에서 username 추출: {}", currentUsername);
            }
        }

        log.info("작성자 확인 - QnA writer: {}, 현재 userId: {}, 현재 username: {}",
                qnaWriter, currentUserId, currentUsername);

        // qnaWriter가 userId 또는 username과 일치하면 작성자로 인정
        boolean isMatch = qnaWriter.equals(currentUserId) ||
                (currentUsername != null && qnaWriter.equals(currentUsername));

        log.info("작성자 일치 여부: {}", isMatch);
        return isMatch;
    }

    /**
     * QnA 엔티티를 DTO로 변환
     */
    private QnaDTO convertToDTO(Qna qna) {
        QnaDTO dto = new QnaDTO();
        dto.setId(qna.getId());
        dto.setTitle(qna.getTitle());
        dto.setWriter(qna.getWriter());
        dto.setQuestion(qna.getQuestion());
        dto.setCreatedAt(qna.getCreatedAt());
        dto.setIsPrivate(qna.getIsPrivate());

        if (qna.getProduct() != null) {
            dto.setProductId(qna.getProduct().getId());
            dto.setProductName(qna.getProduct().getName());
        }

        // 답글 변환
        if (qna.getReplies() != null && !qna.getReplies().isEmpty()) {
            List<QnaReplyDTO> replyDTOList = qna.getReplies().stream()
                    .map(this::convertReplyToDTO)
                    .collect(Collectors.toList());
            dto.setReplies(replyDTOList);
        }



// ...

        if (qna.getImages() != null && !qna.getImages().isEmpty()) {
            List<QnaImageDTO> imageDTOList = qna.getImages().stream()
                    .sorted(Comparator.comparing(QnaImage::getId)) // ✅ ID 기준 오름차순 정렬 추가
                    .map(img -> {
                        QnaImageDTO dtoImg = new QnaImageDTO();
                        dtoImg.setId(img.getId());
                        dtoImg.setImageUrl(img.getImageUrl());
                        return dtoImg;
                    })
                    .collect(Collectors.toList());
            dto.setImages(imageDTOList);
        }

        return dto;
    }

    /**
     * QnaReply 엔티티를 DTO로 변환
     */
    private QnaReplyDTO convertReplyToDTO(QnaReply reply) {
        QnaReplyDTO dto = new QnaReplyDTO();
        dto.setId(reply.getId());
        dto.setContent(reply.getContent());
        dto.setAuthor(reply.getResponder());
        dto.setResponder(reply.getResponder());
        dto.setCreatedAt(reply.getCreatedAt());
        if (reply.getQna() != null) {
            dto.setQnaId(reply.getQna().getId());
        }
        return dto;
    }
}