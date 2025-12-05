package com.onandhome.user;

import com.onandhome.admin.adminProduct.ProductService;
import com.onandhome.admin.adminProduct.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user/product")
@RequiredArgsConstructor
@Slf4j
public class UserProductController {
    
    private final ProductService productService;
    
    /**
     * 카테고리별 상품 목록 페이지
     * GET /user/product/category
     */
    @GetMapping("/category")
    public String productCategory(
            @RequestParam(required = false) String category,
            Model model) {
        
        try {
            log.debug("카테고리 페이지 요청: {}", category);
            
            if (category != null && !category.isEmpty()) {
                model.addAttribute("category", category);
                log.info("카테고리: {}", category);
            } else {
                model.addAttribute("category", "전체");
            }
            
            return "user/product/category";
            
        } catch (Exception e) {
            log.error("카테고리 페이지 로드 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "페이지를 불러올 수 없습니다.");
            return "user/product/category";
        }
    }
    
    /**
     * ✅ 상품 검색 페이지 (상품명 검색)
     * GET /user/product/search
     */
    @GetMapping("/search")
    public String productSearch(
            @RequestParam String keyword,
            Model model) {
        
        try {
            log.debug("상품 검색 페이지 요청: {}", keyword);
            
            model.addAttribute("keyword", keyword);
            model.addAttribute("category", "검색 결과: " + keyword);
            
            return "user/product/category";
            
        } catch (Exception e) {
            log.error("검색 페이지 로드 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "검색 결과를 불러올 수 없습니다.");
            return "user/product/category";
        }
    }
    
    /**
     * 상품 상세 페이지
     * GET /user/product/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        try {
            log.debug("상품 상세 조회: {}", id);
            var productOptional = productService.getById(id);
            
            if (productOptional.isPresent()) {
                model.addAttribute("product", productOptional.get());
                return "user/product/detail";
            } else {
                model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
                return "user/product/detail";
            }
        } catch (Exception e) {
            log.error("상품 상세 조회 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "상품 정보를 불러올 수 없습니다.");
            return "user/product/detail";
        }
    }
    
    /**
     * 카테고리별 상품 조회 API (AJAX용)
     * GET /user/product/api/category/{category}
     */
    @GetMapping("/api/category/{category}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable String category) {
        try {
            log.debug("API - 카테고리별 상품 조회: {}", category);
            List<Product> products = productService.getByCategory(category);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("category", category);
            response.put("products", products);
            response.put("count", products.size());
            
            log.info("카테고리 '{}' - 상품 {} 개 조회", category, products.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("API - 상품 조회 중 오류: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 모든 상품 조회 API
     * GET /user/product/api/all
     */
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        try {
            log.debug("API - 모든 상품 조회");
            List<Product> products = productService.listAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("products", products);
            response.put("count", products.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("API - 상품 조회 중 오류: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 상품 검색 API
     * GET /user/product/api/search
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam String keyword) {
        try {
            log.debug("API - 상품 검색: {}", keyword);
            List<Product> products = productService.search(keyword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keyword", keyword);
            response.put("products", products);
            response.put("count", products.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("API - 상품 검색 중 오류: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
