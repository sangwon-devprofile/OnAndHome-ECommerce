package com.onandhome.email.service;

import com.onandhome.email.entity.AccountDeletion;
import com.onandhome.email.entity.EmailVerification;
import com.onandhome.email.entity.PasswordReset;
import com.onandhome.email.repository.AccountDeletionRepository;
import com.onandhome.email.repository.EmailVerificationRepository;
import com.onandhome.email.repository.PasswordResetRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailVerificationRepository verificationRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final AccountDeletionRepository accountDeletionRepository;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${email.verification.expiration}")
    private Long expirationTime;
    
    /**
     * 6자리 랜덤 인증 코드 생성
     */
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
    
    /**
     * 인증 코드 이메일 전송
     */
    @Transactional
    public void sendVerificationEmail(String toEmail) throws MessagingException {
        // 인증 코드 생성
        String code = generateVerificationCode();
        
        // DB에 저장
        EmailVerification verification = EmailVerification.builder()
                .email(toEmail)
                .code(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(expirationTime / 1000))
                .verified(false)
                .build();
        
        verificationRepository.save(verification);
        
        // 이메일 전송
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("[On&Home] 이메일 인증 코드");
        
        String htmlContent = buildEmailContent(code);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
        
        log.info("인증 코드 이메일 전송 완료: {}", toEmail);
    }
    
    /**
     * 인증 코드 검증
     */
    @Transactional
    public boolean verifyCode(String email, String code) {
        return verificationRepository.findByEmailAndCode(email, code)
                .map(verification -> {
                    if (verification.isExpired()) {
                        log.warn("만료된 인증 코드: {}", email);
                        return false;
                    }
                    if (verification.isVerified()) {
                        log.warn("이미 사용된 인증 코드: {}", email);
                        return false;
                    }
                    
                    verification.verify();
                    verificationRepository.save(verification);
                    log.info("이메일 인증 성공: {}", email);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("유효하지 않은 인증 코드: {}", email);
                    return false;
                });
    }
    
    /**
     * 이메일 인증 완료 여부 확인
     */
    public boolean isEmailVerified(String email) {
        return verificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }
    
    /**
     * 만료된 인증 코드 삭제 (스케줄러에서 호출)
     */
    @Transactional
    public void deleteExpiredVerifications() {
        verificationRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("만료된 인증 코드 삭제 완료");
    }
    
    /**
     * 비밀번호 재설정 코드 이메일 전송
     */
    @Transactional
    public void sendPasswordResetEmail(String toEmail) throws MessagingException {
        // 인증 코드 생성
        String code = generateVerificationCode();
        
        // DB에 저장
        PasswordReset passwordReset = PasswordReset.builder()
                .email(toEmail)
                .code(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(expirationTime / 1000))
                .verified(false)
                .build();
        
        passwordResetRepository.save(passwordReset);
        
        // 이메일 전송
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("[On&Home] 비밀번호 재설정 인증 코드");
        
        String htmlContent = buildPasswordResetEmailContent(code);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
        
        log.info("비밀번호 재설정 코드 이메일 전송 완료: {}", toEmail);
    }
    
    /**
     * 비밀번호 재설정 코드 검증
     */
    @Transactional
    public boolean verifyPasswordResetCode(String email, String code) {
        return passwordResetRepository.findByEmailAndCode(email, code)
                .map(passwordReset -> {
                    if (passwordReset.isExpired()) {
                        log.warn("만료된 비밀번호 재설정 코드: {}", email);
                        return false;
                    }
                    if (passwordReset.isVerified()) {
                        log.warn("이미 사용된 비밀번호 재설정 코드: {}", email);
                        return false;
                    }
                    
                    passwordReset.verify();
                    passwordResetRepository.save(passwordReset);
                    log.info("비밀번호 재설정 코드 인증 성공: {}", email);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("유효하지 않은 비밀번호 재설정 코드: {}", email);
                    return false;
                });
    }
    
    /**
     * 비밀번호 재설정 이메일 HTML 컨텐츠 생성
     */
    private String buildPasswordResetEmailContent(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                "        .code-box { background: white; border: 2px dashed #667eea; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }" +
                "        .code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; }" +
                "        .footer { text-align: center; margin-top: 20px; color: #999; font-size: 12px; }" +
                "        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>비밀번호 재설정</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>안녕하세요!</p>" +
                "            <p><strong>On&Home</strong> 비밀번호 재설정을 위한 인증 코드입니다.</p>" +
                "            <p>아래 인증 코드를 입력하여 본인 확인을 완료해주세요.</p>" +
                "            " +
                "            <div class='code-box'>" +
                "                <div>인증 코드</div>" +
                "                <div class='code'>" + code + "</div>" +
                "            </div>" +
                "            " +
                "            <div class='warning'>" +
                "                <p style='margin: 0;'><strong>⚠️ 주의사항</strong></p>" +
                "                <ul style='margin: 10px 0 0 0; padding-left: 20px;'>" +
                "                    <li>이 인증 코드는 <strong>5분간</strong> 유효합니다.</li>" +
                "                    <li>본인이 요청하지 않은 경우, 이 이메일을 무시하세요.</li>" +
                "                    <li>인증 코드를 타인에게 공유하지 마세요.</li>" +
                "                </ul>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>본 메일은 발신 전용입니다.</p>" +
                "            <p>&copy; 2025 On&Home. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * 이메일 HTML 컨텐츠 생성
     */
    private String buildEmailContent(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                "        .code-box { background: white; border: 2px dashed #667eea; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }" +
                "        .code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; }" +
                "        .footer { text-align: center; margin-top: 20px; color: #999; font-size: 12px; }" +
                "        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>On&Home 이메일 인증</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>안녕하세요!</p>" +
                "            <p><strong>On&Home</strong> 회원가입을 위한 이메일 인증 코드입니다.</p>" +
                "            <p>아래 인증 코드를 입력하여 이메일 인증을 완료해주세요.</p>" +
                "            " +
                "            <div class='code-box'>" +
                "                <div>인증 코드</div>" +
                "                <div class='code'>" + code + "</div>" +
                "            </div>" +
                "            " +
                "            <div class='warning'>" +
                "                <p style='margin: 0;'><strong>⚠️ 주의사항</strong></p>" +
                "                <ul style='margin: 10px 0 0 0; padding-left: 20px;'>" +
                "                    <li>이 인증 코드는 <strong>5분간</strong> 유효합니다.</li>" +
                "                    <li>본인이 요청하지 않은 경우, 이 이메일을 무시하세요.</li>" +
                "                    <li>인증 코드를 타인에게 공유하지 마세요.</li>" +
                "                </ul>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>본 메일은 발신 전용입니다.</p>" +
                "            <p>&copy; 2025 On&Home. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
    
    /**
     * 회원탈퇴 인증 코드 이메일 전송
     */
    @Transactional
    public void sendAccountDeletionEmail(String toEmail) throws MessagingException {
        // 인증 코드 생성
        String code = generateVerificationCode();
        
        // DB에 저장
        AccountDeletion accountDeletion = AccountDeletion.builder()
                .email(toEmail)
                .code(code)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(expirationTime / 1000))
                .verified(false)
                .build();
        
        accountDeletionRepository.save(accountDeletion);
        
        // 이메일 전송
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("[On&Home] 회원탈퇴 인증 코드");
        
        String htmlContent = buildAccountDeletionEmailContent(code);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
        
        log.info("회원탈퇴 인증 코드 이메일 전송 완료: {}", toEmail);
    }
    
    /**
     * 회원탈퇴 인증 코드 검증
     */
    @Transactional
    public boolean verifyAccountDeletionCode(String email, String code) {
        return accountDeletionRepository.findByEmailAndCode(email, code)
                .map(accountDeletion -> {
                    if (accountDeletion.isExpired()) {
                        log.warn("만료된 회원탈퇴 인증 코드: {}", email);
                        return false;
                    }
                    if (accountDeletion.isVerified()) {
                        log.warn("이미 사용된 회원탈퇴 인증 코드: {}", email);
                        return false;
                    }
                    
                    accountDeletion.verify();
                    accountDeletionRepository.save(accountDeletion);
                    log.info("회원탈퇴 인증 성공: {}", email);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("유효하지 않은 회원탈퇴 인증 코드: {}", email);
                    return false;
                });
    }
    
    /**
     * 회원탈퇴 이메일 HTML 컨텐츠 생성
     */
    private String buildAccountDeletionEmailContent(String code) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                "        .header { background: linear-gradient(135deg, #dc3545 0%, #c82333 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                "        .code-box { background: white; border: 2px dashed #dc3545; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }" +
                "        .code { font-size: 32px; font-weight: bold; color: #dc3545; letter-spacing: 5px; }" +
                "        .footer { text-align: center; margin-top: 20px; color: #999; font-size: 12px; }" +
                "        .warning { background: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>⚠️ 회원탈퇴 인증</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>안녕하세요!</p>" +
                "            <p><strong>On&Home</strong> 회원탈퇴를 위한 인증 코드입니다.</p>" +
                "            <p>아래 인증 코드를 입력하여 본인 확인을 완료해주세요.</p>" +
                "            " +
                "            <div class='code-box'>" +
                "                <div>인증 코드</div>" +
                "                <div class='code'>" + code + "</div>" +
                "            </div>" +
                "            " +
                "            <div class='warning'>" +
                "                <p style='margin: 0;'><strong>⚠️ 주의사항</strong></p>" +
                "                <ul style='margin: 10px 0 0 0; padding-left: 20px;'>" +
                "                    <li>이 인증 코드는 <strong>5분간</strong> 유효합니다.</li>" +
                "                    <li>회원탈퇴 시 모든 데이터가 <strong>영구적으로 삭제</strong>됩니다.</li>" +
                "                    <li>본인이 요청하지 않은 경우, 즉시 비밀번호를 변경하세요.</li>" +
                "                    <li>인증 코드를 타인에게 공유하지 마세요.</li>" +
                "                </ul>" +
                "            </div>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>본 메일은 발신 전용입니다.</p>" +
                "            <p>&copy; 2025 On&Home. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }
}
