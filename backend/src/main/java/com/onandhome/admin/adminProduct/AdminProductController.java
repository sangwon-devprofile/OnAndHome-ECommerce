package com.onandhome.admin.adminProduct;

import com.onandhome.admin.adminProduct.dto.ProductDTO;
import com.onandhome.admin.adminProduct.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/product")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {
    
    private final ProductService productService;
    
    /**
     * 상품 목록 페이지
     * GET /admin/product/list
     */
    @GetMapping("/list")
    public String list(Model model) {
        try {
            log.debug("상품 목록 페이지 요청");
            List<Product> products = productService.listAll();
            model.addAttribute("products", products);
            model.addAttribute("productCount", products.size());
            return "admin/product/list";
        } catch (Exception e) {
            log.error("상품 목록 페이지 로드 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "상품 목록을 불러올 수 없습니다.");
            return "admin/product/list";
        }
    }
    
    /**
     * 상품 등록 페이지
     * GET /admin/product/create
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        try {
            log.debug("상품 등록 페이지 요청");
            model.addAttribute("product", new ProductDTO());
            return "admin/product/create";
        } catch (Exception e) {
            log.error("상품 등록 페이지 로드 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "상품 등록 페이지를 불러올 수 없습니다.");
            return "admin/product/create";
        }
    }
    
    /**
     * 상품 등록 처리
     * POST /admin/product/create
     */
    @PostMapping("/create")
    public String create(@ModelAttribute ProductDTO productDTO, Model model) {
        try {
            log.info("상품 등록 요청: {}", productDTO.getName());
            
            if (productDTO.getName() == null || productDTO.getName().isEmpty()) {
                model.addAttribute("errorMessage", "상품명은 필수입니다.");
                model.addAttribute("product", productDTO);
                return "admin/product/create";
            }
            
            ProductDTO createdProduct = productService.create(productDTO);
            log.info("상품 등록 성공: {} (ID: {})", createdProduct.getName(), createdProduct.getId());
            
            return "redirect:/admin/product/list";
        } catch (IllegalArgumentException e) {
            log.warn("상품 등록 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("product", productDTO);
            return "admin/product/create";
        } catch (Exception e) {
            log.error("상품 등록 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "상품 등록 중 오류가 발생했습니다.");
            model.addAttribute("product", productDTO);
            return "admin/product/create";
        }
    }
    
    /**
     * 상품 수정 페이지
     * GET /admin/product/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            log.debug("상품 수정 페이지 요청: {}", id);
            Optional<ProductDTO> product = productService.getById(id);
            
            if (product.isPresent()) {
                model.addAttribute("product", product.get());
                return "admin/product/edit";
            } else {
                model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
                return "redirect:/admin/product/list";
            }
        } catch (Exception e) {
            log.error("상품 수정 페이지 로드 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "상품 정보를 불러올 수 없습니다.");
            return "redirect:/admin/product/list";
        }
    }
    
    /**
     * 상품 수정 처리
     * POST /admin/product/edit/{id}
     */
    @PostMapping("/edit/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute ProductDTO productDTO,
            Model model) {
        try {
            log.info("상품 수정 요청: ID {}", id);
            
            if (productDTO.getName() == null || productDTO.getName().isEmpty()) {
                model.addAttribute("errorMessage", "상품명은 필수입니다.");
                model.addAttribute("product", productDTO);
                model.addAttribute("isEdit", true);
                return "admin/product/create";
            }
            
            ProductDTO updatedProduct = productService.update(id, productDTO);
            log.info("상품 수정 성공: {} (ID: {})", updatedProduct.getName(), id);
            
            return "redirect:/admin/product/list";
        } catch (IllegalArgumentException e) {
            log.warn("상품 수정 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("product", productDTO);
            model.addAttribute("isEdit", true);
            return "admin/product/create";
        } catch (Exception e) {
            log.error("상품 수정 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "상품 수정 중 오류가 발생했습니다.");
            model.addAttribute("product", productDTO);
            model.addAttribute("isEdit", true);
            return "admin/product/create";
        }
    }
    
    /**
     * 상품 삭제 처리
     * POST /admin/product/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, Model model) {
        try {
            log.info("상품 삭제 요청: ID {}", id);
            productService.delete(id);
            log.info("상품 삭제 성공: ID {}", id);
            
            return "redirect:/admin/product/list";
        } catch (IllegalArgumentException e) {
            log.warn("상품 삭제 실패: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/product/list";
        } catch (Exception e) {
            log.error("상품 삭제 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "상품 삭제 중 오류가 발생했습니다.");
            return "redirect:/admin/product/list";
        }
    }
    
    /**
     * 상품 상세 페이지
     * GET /admin/product/{id}
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            log.debug("상품 상세 조회: {}", id);
            Optional<ProductDTO> product = productService.getById(id);
            
            if (product.isPresent()) {
                model.addAttribute("product", product.get());
                return "admin/product/detail";
            } else {
                model.addAttribute("errorMessage", "존재하지 않는 상품입니다.");
                return "redirect:/admin/product/list";
            }
        } catch (Exception e) {
            log.error("상품 상세 조회 중 오류: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "상품 정보를 불러올 수 없습니다.");
            return "redirect:/admin/product/list";
        }
    }
}
