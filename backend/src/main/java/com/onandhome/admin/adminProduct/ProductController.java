package com.onandhome.admin.adminProduct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.onandhome.admin.adminProduct.dto.ProductDTO;
import com.onandhome.admin.adminProduct.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
// @RestController: 이 클래스가 REST API 컨트롤러임을 Spring에게 알림
// 모든 메서드의 리턴값이 HTTP Response Body에 직접 작성됨
// 자동으로 JSON 변환됨
@RequestMapping("/api/products")
// @RequestMapping: 이 컨트롤러의 기본 URL 경로 설정
// 모든 메서드의 URL은 /api/products로 시작
@RequiredArgsConstructor
// Lombok 어노테이션
// final 필드에 대한 생성자를 자동으로 생성
// ProductService productService에 대한 생성자 주입
@Slf4j
// Lombok 어노테이션
// log 객체를 자동으로 생성 (로깅용)
// log.info(), log.error() 등을 사용 가능
public class ProductController {

	private final ProductService productService;
    // productService: 비즈니스 로직을 처리하는 서비스 객체
    // final: 한 번 할당되면 변경 불가 (생성자 주입 시 할당됨)
    // Spring이 자동으로 ProductService 객체를 주입(Dependency Injection)

	/**
	 * 모든 상품 조회
	 * GET /api/products/list
	 */
	@GetMapping("/list")
    // @GetMapping: HTTP GET 요청을 처리하는 메서드
    // "/list": URL 경로 (@RequestMapping과 합쳐져 /api/products/list)
    // 프론트엔드의 fetch 요청이 이 메서드로 들어옴
	public ResponseEntity<Map<String, Object>> list() {
        // ResponseEntity: HTTP 응답을 나타내는 클래스
        //   - 상태 코드 (200, 404, 500 등)
        //   - 헤더
        //   - 바디
        // Map<String, Object>: 응답 바디의 타입
        //   - String: 키 (예: "success", "data", "count")
        //   - Object: 값 (다양한 타입 가능)
        // list(): 메서드명 (파라미터 없음)
		Map<String, Object> response = new HashMap<>();
        // response: 응답 데이터를 담을 Map 객체 생성
        // HashMap: Map 인터페이스의 구현체
        // new HashMap<>(): 빈 HashMap 객체 생성
		try {
			List<Product> products = productService.listAll();
            // productService.listAll(): 서비스의 listAll() 메서드 호출
            // 모든 상품을 조회하는 비즈니스 로직 실행
            // List<Product>: Product 엔티티의 리스트
            // products: 조회된 상품 리스트를 저장하는 변수
			response.put("success", true);
            // response.put(): Map에 키-값 쌍 추가
            // "success": 키
            // true: 값 (성공 여부)
			response.put("data", products);
            // "data": 키
            // products: 값 (상품 리스트)
            // products는 자동으로 JSON 배열로 변환됨
			response.put("count", products.size());
            // "count": 키
            // products.size(): 리스트의 크기 (상품 개수)
			return ResponseEntity.ok(response);
            // ResponseEntity.ok(): HTTP 200 OK 응답 생성
            // response: 응답 바디 (JSON으로 자동 변환)
            // 최종 응답: { "success": true, "data": [...], "count": 6 }
		} catch (Exception e) {
            // catch: try 블록에서 예외 발생 시 실행
            // Exception e: 발생한 예외 객체
			log.error("상품 목록 조회 중 오류: {}", e.getMessage());
			response.put("success", false);
			response.put("message", "상품 목록 조회 중 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            // ResponseEntity.status(): 특정 HTTP 상태 코드로 응답 생성
            // HttpStatus.INTERNAL_SERVER_ERROR: HTTP 500 에러
            // .body(response): 응답 바디 설정
		}
	}

	/**
	 * ID로 상품 조회
	 * GET /api/products/{id}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        // @GetMapping("/{id}"): GET /api/products/{id} 매핑
        // @PathVariable Long id: URL의 {id} 추출
        // GET /api/products/423 → id = 423
		Map<String, Object> response = new HashMap<>();
        // 응답 데이터를 담을 Map
		try {
			Optional<ProductDTO> product = productService.getById(id);
            // productService.getById(id): 상품 조회 서비스 호출
			if (product.isPresent()) {
				response.put("success", true);
				response.put("data", product.get());
				return ResponseEntity.ok(response);
                // HTTP 200 OK + JSON 응답
			} else {
                // product가 없으면
				response.put("success", false);
				response.put("message", "존재하지 않는 상품입니다.");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                // HTTP 404 NOT FOUND
			}
		} catch (Exception e) {
			log.error("상품 조회 중 오류: {}", e.getMessage());
			response.put("success", false);
			response.put("message", "상품 조회 중 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            // HTTP 500 ERROR
		}
	}

	/**
	 * 상품 생성 (DTO 사용) ✅ Talent 테스트용
	 * POST /api/products/create
	 *
	 * 요청 예시 (JSON):
	 * {
	 *     "name": "무선 이어폰",
	 *     "description": "고음질 무선 이어폰입니다.",
	 *     "price": 59900,
	 *     "stock": 100,
	 *     "thumbnailImage": "https://example.com/thumbnail.jpg",
	 *     "detailImage": "https://example.com/detail.jpg"
	 * }
	 */
	@PostMapping("/create")
	public ResponseEntity<Map<String, Object>> create(@RequestBody ProductDTO productDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("상품 생성 요청: {}", productDTO.getName());

			ProductDTO createdProduct = productService.create(productDTO);
			response.put("success", true);
			response.put("message", "상품이 성공적으로 생성되었습니다.");
			response.put("data", createdProduct);

			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException e) {
			log.warn("상품 생성 실패: {}", e.getMessage());
			response.put("success", false);
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		} catch (Exception e) {
			log.error("상품 생성 중 오류: {}", e.getMessage());
			response.put("success", false);
			response.put("message", "상품 생성 중 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	/**
	 * 상품 수정
	 * PUT /api/products/{id}
	 *
	 * 요청 예시 (JSON):
	 * {
	 *     "name": "무선 이어폰 프로",
	 *     "description": "고음질 무선 이어폰 프로 버전입니다.",
	 *     "price": 79900,
	 *     "stock": 150,
	 *     "thumbnailImage": "https://example.com/thumbnail_pro.jpg",
	 *     "detailImage": "https://example.com/detail_pro.jpg"
	 * }
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Map<String, Object>> update(
			@PathVariable Long id,
			@RequestBody ProductDTO productDTO) {
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("상품 수정 요청: ID {}", id);

			ProductDTO updatedProduct = productService.update(id, productDTO);
			response.put("success", true);
			response.put("message", "상품이 성공적으로 수정되었습니다.");
			response.put("data", updatedProduct);

			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			log.warn("상품 수정 실패: {}", e.getMessage());
			response.put("success", false);
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		} catch (Exception e) {
			log.error("상품 수정 중 오류: {}", e.getMessage());
			response.put("success", false);
			response.put("message", "상품 수정 중 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	/**
	 * 상품 삭제
	 * DELETE /api/products/{id}
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("상품 삭제 요청: ID {}", id);

			productService.delete(id);
			response.put("success", true);
			response.put("message", "상품이 성공적으로 삭제되었습니다.");

			return ResponseEntity.ok(response);
		} catch (IllegalArgumentException e) {
			log.warn("상품 삭제 실패: {}", e.getMessage());
			response.put("success", false);
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		} catch (Exception e) {
			log.error("상품 삭제 중 오류: {}", e.getMessage());
			response.put("success", false);
			response.put("message", "상품 삭제 중 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}

	/**
	 * 상품 검색
	 * GET /api/products/search?keyword=검색어
	 */
	@GetMapping("/search")
	public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword) {
		Map<String, Object> response = new HashMap<>();
		try {
			log.info("상품 검색 요청: {}", keyword);

			List<Product> products = productService.search(keyword);
			response.put("success", true);
			response.put("data", products);
			response.put("count", products.size());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("상품 검색 중 오류: {}", e.getMessage());
			response.put("success", false);
			response.put("message", "상품 검색 중 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}
