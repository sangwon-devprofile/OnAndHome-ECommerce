package com.onandhome.admin.adminProduct;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * 파일 업로드 처리
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 업로드 디렉토리 생성
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // 원본 파일명
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        // 파일 확장자 추출
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }

        // 고유한 파일명 생성 (UUID + 확장자)
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // 파일 저장 경로
        Path filePath = Paths.get(uploadDir, uniqueFilename);

        // 파일 저장
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("파일 업로드 완료: {}", uniqueFilename);

        // 웹에서 접근 가능한 URL 반환
        return "/uploads/" + uniqueFilename;
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        // URL에서 파일명 추출
        String filename = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        Path filePath = Paths.get(uploadDir, filename);

        try {
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 완료: {}", filename);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filename, e);
        }
    }
}
