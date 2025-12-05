package com.onandhome.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.onandhome.notification.entity.Notification;
import com.onandhome.user.entity.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 1) 사용자의 모든 알림 조회 (최신순 정렬)
    // 사용자의 알림 목록 페이지에서 사용됨.
    // 최신 알림이 위에 오도록 createdAt 기준 내림차순 정렬.
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // 2) 읽지 않은 알림 수 조회
    // 알림 아이콘(벨) 표시할 때 "읽지 않은 알림 개수"를 띄우는 데 사용됨.
    long countByUserAndIsReadFalse(User user);

    // 3) 사용자의 읽지 않은 알림 전체 조회 (최신순)
    // "안 읽은 알림 보기" 기능이나 실시간 알림 도착 후 목록 업데이트에 사용됨.
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);


    // 4) 특정 타입 + referenceId 를 기준으로 알림 삭제
    // 예: QnA가 삭제되면 해당 QnA 알림도 자동 삭제해야 할 때 사용됨
    // @Modifying + @Transactional → DB 데이터 변경 쿼리이므로 필수.
    @Modifying
    @Transactional
    void deleteByTypeAndReferenceId(String type, Long referenceId);
}

/*
요약
1. NotificationRepository는 알림 데이터를 DB에서 조회·관리하는 핵심 저장소
2. ‘전체 알림 조회’, ‘읽지 않은 알림 조회’, ‘읽지 않은 알림 카운트’를 모두 담당
3. 최근 알림이 위로 오도록 정렬 기능이 포함되어 있음
4. QnA/리뷰/공지 삭제 시 함께 알림 삭제할 수 있도록 조건 삭제 쿼리도 제공
5. 일반 알림과 실시간 알림 기능 모두 이 레포지토리를 기반으로 작동
 */