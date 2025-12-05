package com.onandhome.favorite;

import com.onandhome.admin.adminProduct.ProductRepository;
import com.onandhome.admin.adminProduct.entity.Product;
import com.onandhome.favorite.dto.FavoriteDTO;
import com.onandhome.favorite.entity.Favorite;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//역할: 찜하기 관련 비즈니스 로직 처리

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    @Transactional(readOnly = true)
    //사용자의 찜 목록 조회 기능
    public List<FavoriteDTO> getFavoritesByUserId(Long userId) {
        log.info("찜 목록 조회 - userId: {}", userId);

        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return favorites.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    //찜하기 토글 (있으면 삭제, 없으면 추가)
    public FavoriteDTO toggleFavorite(Long userId, Long productId) {
        log.info("찜하기 토글 - userId: {}, productId: {}", userId, productId);

        // 이미 찜한 상품인지 확인 Optional이용해서 null값 허용
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserIdAndProductId(userId, productId);

        if (existingFavorite.isPresent()) {
            // 이미 찜한 경우 -> 삭제
            favoriteRepository.delete(existingFavorite.get());
            log.info("찜하기 취소 완료 - favoriteId: {}", existingFavorite.get().getId());
            return null; // 삭제했음을 나타내기 위해 null 반환
        } else {
            // 찜하지 않은 경우 -> 추가
            return addFavorite(userId, productId);
        }
    }


    @Transactional
    //찜 하기 추가
    public FavoriteDTO addFavorite(Long userId, Long productId) {
        log.info("찜하기 추가 - userId: {}, productId: {}", userId, productId);

        // 사용자 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 상품 확인
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다. ID: " + productId));

        // 이미 찜한 상품인지 재확인
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new IllegalStateException("이미 찜한 상품입니다.");
        }

        // 찜하기 생성
        Favorite favorite = Favorite.builder()
                .user(user)
                .product(product)
                .build();

        Favorite savedFavorite = favoriteRepository.save(favorite);
        log.info("찜하기 추가 완료 - favoriteId: {}", savedFavorite.getId());

        return convertToDTO(savedFavorite);
    }


    @Transactional
    //찜하기 삭제
    public void removeFavorite(Long userId, Long productId) {
        log.info("찜하기 삭제 - userId: {}, productId: {}", userId, productId);

        Favorite favorite = favoriteRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new IllegalArgumentException("찜 목록에서 찾을 수 없습니다."));

        favoriteRepository.delete(favorite);
        log.info("찜하기 삭제 완료 - favoriteId: {}", favorite.getId());
    }


    @Transactional(readOnly = true)
    //특정 상품이 찜되어 있는지 확인
    public boolean isFavorite(Long userId, Long productId) {
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }


    @Transactional(readOnly = true)
    //특정 상품의 찜 개수 조회
    public long getFavoriteCountByProductId(Long productId) {
        return favoriteRepository.countByProductId(productId);
    }


    @Transactional(readOnly = true)
    //사용자별 찜 개수 조회
    public long getFavoriteCountByUserId(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }

   //FavoriteEntity를 FavoriteDTO로 변환
    private FavoriteDTO convertToDTO(Favorite favorite) {
        Product product = favorite.getProduct();

        return FavoriteDTO.builder()
                .id(favorite.getId())
                .userId(favorite.getUser().getId())
                .productId(product.getId())
                .productName(product.getName())
                .productCode(product.getProductCode())
                .price(product.getPrice())
                .salePrice(product.getSalePrice())
                .thumbnailImage(product.getThumbnailImage())
                .category(product.getCategory())
                .stock(product.getStock())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}