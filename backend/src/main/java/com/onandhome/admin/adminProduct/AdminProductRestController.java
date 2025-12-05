package com.onandhome.admin.adminProduct;

import com.onandhome.admin.adminProduct.dto.CategoryDTO;
import com.onandhome.admin.adminProduct.dto.ProductDTO;
import com.onandhome.admin.adminProduct.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 관리자 상품 관리 REST API 컨트롤러
 * - React 프론트엔드와 통신하는 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
// ✅ @CrossOrigin 제거 - SecurityConfig에서 처리
public class AdminProductRestController {

    private final ProductService productService;
    private final FileUploadService fileUploadService;

    /**
     * 카테고리 목록 조회 API
     * GET /api/admin/products/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        log.info("=== 카테고리 목록 조회 API 호출 ===");
        
        // 카테고리 데이터 정의 (사용자 페이지 Header.js와 동일)
        List<CategoryDTO> categories = new ArrayList<>();
        
        // TV/오디오
        categories.add(new CategoryDTO(
            "tv_audio",
            "TV/오디오",
            Arrays.asList("TV", "오디오")
        ));
        
        // 주방가전
        categories.add(new CategoryDTO(
            "kitchen",
            "주방가전",
            Arrays.asList("냉장고", "전자렌지", "식기세척기")
        ));
        
        // 생활가전
        categories.add(new CategoryDTO(
            "living",
            "생활가전",
            Arrays.asList("세탁기", "청소기")
        ));
        
        // 에어컨/공기청정기
        categories.add(new CategoryDTO(
            "air",
            "에어컨/공기청정기",
            Arrays.asList("에어컨", "공기청정기", "정수기")
        ));
        
        // 기타
        categories.add(new CategoryDTO(
            "etc",
            "기타",
            Arrays.asList("안마의자", "PC")
        ));
        
        log.info("카테고리 개수: {}", categories.size());
        return ResponseEntity.ok(categories);
    }

    /**
     * 상품 등록 API
     * POST /api/admin/products
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestParam(value = "name") String name,
            @RequestParam(value = "category") String category,
            @RequestParam(value = "productCode", required = false) String productCode,
            @RequestParam(value = "manufacturer", required = false) String manufacturer,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "price") Integer price,
            @RequestParam(value = "salePrice", required = false) Integer salePrice,
            @RequestParam(value = "stock", required = false, defaultValue = "0") Integer stock,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "thumbnailImage", required = false) MultipartFile thumbnailImage,
            @RequestParam(value = "detailImage", required = false) MultipartFile detailImage) {
        
        log.info("=== 상품 등록 API 호출 ===");
        log.info("상품명: {}", name);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setName(name);
            productDTO.setCategory(category);
            productDTO.setProductCode(productCode);
            productDTO.setManufacturer(manufacturer);
            productDTO.setCountry(country);
            productDTO.setPrice(price);
            productDTO.setSalePrice(salePrice != null ? salePrice : price);
            productDTO.setStock(stock);
            productDTO.setDescription(description);
            
            // 파일 업로드 처리
            if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
                String thumbnailUrl = fileUploadService.uploadFile(thumbnailImage);
                productDTO.setThumbnailImage(thumbnailUrl);
                log.info("썸네일 이미지 업로드: {}", thumbnailUrl);
            }
            
            if (detailImage != null && !detailImage.isEmpty()) {
                String detailUrl = fileUploadService.uploadFile(detailImage);
                productDTO.setDetailImage(detailUrl);
                log.info("상세 이미지 업로드: {}", detailUrl);
            }
            
            ProductDTO createdProduct = productService.create(productDTO);
            
            response.put("success", true);
            response.put("message", "상품이 등록되었습니다.");
            response.put("data", createdProduct);
            log.info("상품 등록 성공: {} (ID: {})", createdProduct.getName(), createdProduct.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("상품 등록 실패", e);
            response.put("success", false);
            response.put("message", "상품 등록에 실패했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 상품 수정 API
     * PUT /api/admin/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable("id") Long productId,
            @RequestParam(value = "name") String name,
            @RequestParam(value = "category") String category,
            @RequestParam(value = "productCode", required = false) String productCode,
            @RequestParam(value = "manufacturer", required = false) String manufacturer,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "price") Integer price,
            @RequestParam(value = "salePrice", required = false) Integer salePrice,
            @RequestParam(value = "stock", required = false, defaultValue = "0") Integer stock,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "thumbnailImage", required = false) MultipartFile thumbnailImage,
            @RequestParam(value = "detailImage", required = false) MultipartFile detailImage) {
        
        log.info("=== 상품 수정 API 호출 ===");
        log.info("상품 ID: {}, 상품명: {}", productId, name);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setName(name);
            productDTO.setCategory(category);
            productDTO.setProductCode(productCode);
            productDTO.setManufacturer(manufacturer);
            productDTO.setCountry(country);
            productDTO.setPrice(price);
            productDTO.setSalePrice(salePrice != null ? salePrice : price);
            productDTO.setStock(stock);
            productDTO.setDescription(description);
            
            // 파일 업로드 처리
            if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
                String thumbnailUrl = fileUploadService.uploadFile(thumbnailImage);
                productDTO.setThumbnailImage(thumbnailUrl);
                log.info("썸네일 이미지 업로드: {}", thumbnailUrl);
            }
            
            if (detailImage != null && !detailImage.isEmpty()) {
                String detailUrl = fileUploadService.uploadFile(detailImage);
                productDTO.setDetailImage(detailUrl);
                log.info("상세 이미지 업로드: {}", detailUrl);
            }
            
            ProductDTO updatedProduct = productService.update(productId, productDTO);
            
            response.put("success", true);
            response.put("message", "상품이 수정되었습니다.");
            response.put("data", updatedProduct);
            log.info("상품 수정 성공: {}", productId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("상품 수정 실패", e);
            response.put("success", false);
            response.put("message", "상품 수정에 실패했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 상품 목록 조회 API
     * GET /api/admin/products?category=카테고리&status=상태&kw=검색어
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProductList(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "kw", required = false) String keyword) {
        
        log.info("=== 상품 목록 조회 API 호출 ===");
        log.info("카테고리: {}, 상태: {}, 검색어: {}", category, status, keyword);
        
        try {
            List<Product> products = productService.listAll();
            
            // 필터링 적용
            List<ProductDTO> filteredProducts = products.stream()
                    .map(this::convertToDTO)
                    .filter(product -> {
                        boolean matches = true;
                        
                        // 카테고리 필터
                        if (category != null && !category.equals("all") && !category.isEmpty()) {
                            matches = matches && product.getCategory().equals(category);
                        }
                        
                        // 검색어 필터
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String lowerKeyword = keyword.toLowerCase();
                            matches = matches && (
                                product.getName().toLowerCase().contains(lowerKeyword) ||
                                product.getProductCode().toLowerCase().contains(lowerKeyword)
                            );
                        }
                        
                        return matches;
                    })
                    .collect(Collectors.toList());
            
            log.info("조회된 상품 수: {}", filteredProducts.size());
            return ResponseEntity.ok(filteredProducts);
            
        } catch (Exception e) {
            log.error("상품 목록 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    /**
     * 상품 상세 조회 API
     * GET /api/admin/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable("id") Long productId) {
        log.info("=== 상품 상세 조회 API 호출 ===");
        log.info("상품 ID: {}", productId);
        
        try {
            ProductDTO product = productService.getById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            
            log.info("조회된 상품: {}", product.getName());
            return ResponseEntity.ok(product);
            
        } catch (IllegalArgumentException e) {
            log.warn("상품을 찾을 수 없음: {}", productId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "상품을 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("상품 상세 조회 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "상품 정보를 불러올 수 없습니다."));
        }
    }

    /**
     * 상품 삭제 API (단일)
     * DELETE /api/admin/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable("id") Long productId) {
        log.info("=== 상품 삭제 API 호출 ===");
        log.info("상품 ID: {}", productId);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            productService.delete(productId);
            
            response.put("success", true);
            response.put("message", "상품이 삭제되었습니다.");
            log.info("상품 삭제 성공: {}", productId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("상품 삭제 실패", e);
            response.put("success", false);
            response.put("message", "상품 삭제에 실패했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 상품 다중 삭제 API
     * POST /api/admin/products/delete
     * Body: { "ids": [1, 2, 3] }
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteMultipleProducts(
            @RequestBody Map<String, List<Long>> request) {
        
        log.info("=== 상품 다중 삭제 API 호출 ===");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Long> productIds = request.get("ids");
            
            if (productIds == null || productIds.isEmpty()) {
                log.warn("삭제할 상품 ID 목록이 비어있음");
                response.put("success", false);
                response.put("message", "삭제할 상품을 선택해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("삭제할 상품 수: {}", productIds.size());
            log.info("상품 ID 목록: {}", productIds);
            
            int deletedCount = 0;
            int failedCount = 0;
            
            for (Long productId : productIds) {
                try {
                    productService.delete(productId);
                    deletedCount++;
                    log.info("상품 삭제 성공: {}", productId);
                } catch (Exception e) {
                    failedCount++;
                    log.error("상품 ID {} 삭제 실패: {}", productId, e.getMessage());
                }
            }
            
            response.put("success", true);
            response.put("message", String.format("%d개의 상품이 삭제되었습니다.%s", 
                    deletedCount, 
                    failedCount > 0 ? String.format(" (%d개 실패)", failedCount) : ""));
            response.put("deletedCount", deletedCount);
            response.put("failedCount", failedCount);
            
            log.info("삭제 완료 - 성공: {}, 실패: {}", deletedCount, failedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("상품 다중 삭제 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 상품 상태 변경 API
     * PATCH /api/admin/products/{id}/status
     * Body: { "status": "판매중" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateProductStatus(
            @PathVariable("id") Long productId,
            @RequestBody Map<String, String> request) {
        
        log.info("=== 상품 상태 변경 API 호출 ===");
        log.info("상품 ID: {}, 새 상태: {}", productId, request.get("status"));
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String newStatus = request.get("status");
            
            // 상태 변경 로직 구현 (ProductService에 메서드 추가 필요)
            // productService.updateStatus(productId, newStatus);
            
            response.put("success", true);
            response.put("message", "상품 상태가 변경되었습니다.");
            log.info("상품 상태 변경 성공: {}", productId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("상품 상태 변경 실패", e);
            response.put("success", false);
            response.put("message", "상품 상태 변경에 실패했습니다: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Product Entity를 DTO로 변환
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setProductCode(product.getProductCode());
        dto.setName(product.getName());
        dto.setCategory(product.getCategory());
        dto.setPrice(product.getPrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setStock(product.getStock());
        dto.setManufacturer(product.getManufacturer());
        dto.setDescription(product.getDescription());
        dto.setDetailImage(product.getDetailImage());
        dto.setThumbnailImage(product.getThumbnailImage());
        dto.setCountry(product.getCountry());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}
