// ===================================================================
// 주의: 이 클래스는 더 이상 사용되지 않습니다.
// CartService와 CartBusinessService로 기능이 이동했습니다.
// ===================================================================

/*
package com.onandhome.cart;

import java.util.List;
import java.util.Optional;

import com.onandhome.cart.entity.CartItem;
import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.admin.adminProduct.ProductRepository;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CartController {
    private final CartItemRepository cartRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;

    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return List.of();
        return cartRepo.findByUser(user);
    }

    @Transactional
    public CartItem addToCart(Long userId, Long productId, int qty) {
        User user = userRepo.findById(userId).orElseThrow();
        Product p = productRepo.findById(productId).orElseThrow();
        Optional<CartItem> existing = cartRepo.findByUserAndProduct(user, p);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + Math.max(qty, 1));
            return cartRepo.save(item);
        }
        CartItem item = new CartItem();
        item.setUser(user);
        item.setProduct(p);
        item.setQuantity(Math.max(qty, 1));
        return cartRepo.save(item);
    }

    @Transactional
    public CartItem updateQuantity(Long userId, Long productId, int qty) {
        User user = userRepo.findById(userId).orElseThrow();
        Product p = productRepo.findById(productId).orElseThrow();
        CartItem item = cartRepo.findByUserAndProduct(user, p).orElseThrow();
        item.setQuantity(Math.max(qty, 1));
        return cartRepo.save(item);
    }

    @Transactional
    public void removeItem(Long cartItemId) {
        cartRepo.deleteById(cartItemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        cartRepo.deleteByUser(user);
    }
}
*/
