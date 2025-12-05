package com.onandhome.util;

/**
 * JWT 토큰 처리 중 발생하는 예외를 처리하기 위한 커스텀 예외 클래스
 * RuntimeException을 상속받아 Unchecked Exception으로 동작
 * 
 * 발생 위치: JWTUtil.validateToken() 메소드
 * 처리 위치: JWTCheckFilter.doFilterInternal() 메소드의 catch 블록
 * 
 * 예외 메시지 종류:
 * - "MalFormed": JWT 형식이 잘못된 경우
 * - "Expired": JWT 만료 시간이 지난 경우
 * - "JWTError": 서명 불일치 등 기타 JWT 관련 에러
 * - "Error": 예상치 못한 에러
 */
public class CustomJWTException extends RuntimeException {
    
    /**
     * 에러 메시지를 포함한 예외 생성자
     * @param msg - 에러 메시지 ("MalFormed", "Expired", "JWTError", "Error" 중 하나)
     *            이 메시지는 JWTCheckFilter에서 catch되어 HTTP 응답 body에 포함됨
     *            프론트엔드에서 이 메시지를 확인하여 토큰 재발급 또는 로그인 페이지 이동 처리
     */
    public CustomJWTException(String msg) {
        super(msg); // 부모 클래스(RuntimeException)의 생성자 호출
    }
}
