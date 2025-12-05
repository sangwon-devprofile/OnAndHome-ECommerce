package com.onandhome.advertisement;

import com.onandhome.advertisement.dto.AdvertisementDTO;
import com.onandhome.advertisement.entity.Advertisement;
import com.onandhome.notification.NotificationService;
import com.onandhome.user.UserRepository;
import com.onandhome.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 광고 목록 조회
    @Transactional(readOnly = true)
    public List<AdvertisementDTO> getAllAdvertisements() {
        return advertisementRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(AdvertisementDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 활성 광고 목록 조회
    @Transactional(readOnly = true)
    public List<AdvertisementDTO> getActiveAdvertisements() {
        return advertisementRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(AdvertisementDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 광고 상세 조회
    @Transactional(readOnly = true)
    public AdvertisementDTO getAdvertisement(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("광고를 찾을 수 없습니다."));
        return AdvertisementDTO.fromEntity(advertisement);
    }

    // 광고 생성
    public AdvertisementDTO createAdvertisement(AdvertisementDTO dto) {
        Advertisement advertisement = Advertisement.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .linkUrl(dto.getLinkUrl())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        Advertisement saved = advertisementRepository.save(advertisement);
        log.info("광고 생성 완료: id={}, title={}", saved.getId(), saved.getTitle());

        return AdvertisementDTO.fromEntity(saved);
    }

    // 광고 수정
    public AdvertisementDTO updateAdvertisement(Long id, AdvertisementDTO dto) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("광고를 찾을 수 없습니다."));

        advertisement.setTitle(dto.getTitle());
        advertisement.setContent(dto.getContent());
        advertisement.setImageUrl(dto.getImageUrl());
        advertisement.setLinkUrl(dto.getLinkUrl());
        advertisement.setActive(dto.getActive());

        Advertisement updated = advertisementRepository.save(advertisement);
        log.info("광고 수정 완료: id={}", id);

        return AdvertisementDTO.fromEntity(updated);
    }

    // 광고 삭제
    public void deleteAdvertisement(Long id) {
        Advertisement advertisement = advertisementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("광고를 찾을 수 없습니다."));

        advertisementRepository.delete(advertisement);
        log.info("광고 삭제 완료: id={}", id);
    }

    // 광고 알림 발송 (DB + WebSocket)
    public int sendAdvertisementNotification(Long advertisementId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new RuntimeException("광고를 찾을 수 없습니다."));

        // 마케팅 동의한 사용자 목록 조회
        List<User> marketingUsers = userRepository.findByMarketingConsentTrueAndRoleNot(0);

        log.info("광고 알림 발송 시작: id={}, 대상 {}명 (DB + WebSocket)", advertisementId, marketingUsers.size());

        // WebSocket 알림 데이터 생성
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "MARKETING");
        notification.put("advertisementId", advertisementId);
        notification.put("title", advertisement.getTitle());
        notification.put("message", advertisement.getContent());
        notification.put("timestamp", LocalDateTime.now().toString());

        int successCount = 0;
        for (User user : marketingUsers) {
            try {
                // DB에 알림 저장
                notificationService.createNotification(
                        user.getUserId(),
                        advertisement.getTitle(),
                        advertisement.getContent(),
                        "MARKETING",
                        advertisementId,
                        null
                );

                // WebSocket으로 실시간 알림 전송
                messagingTemplate.convertAndSendToUser(
                        user.getUserId(),
                        "/queue/notifications",
                        notification
                );

                successCount++;
            } catch (Exception e) {
                log.error("사용자 {}에게 광고 알림 전송 실패: {}", user.getUserId(), e.getMessage());
            }
        }

        // 발송 일시 기록
        advertisement.setSentAt(LocalDateTime.now());
        advertisementRepository.save(advertisement);

        log.info("광고 알림 발송 완료 (DB + WebSocket): id={}, 발송 수={}/{}", advertisementId, successCount, marketingUsers.size());

        return successCount;
    }
}

/*
요약
1. 광고 알림은 사용자 DB 저장 알림과 WebSocket 실시간 알림을 동시에 전송
2. 마케팅 수신 동의한 일반 사용자에게만 광고 알림이 감
3. WebSocket 메시지는 /user/{userId}/queue/notifications 경로로 전송됨
4. DB 저장은 NotificationService.createNotification()에서 처리됨
5. 전송 성공/오류는 관리자 로그로 모두 기록됨
 */