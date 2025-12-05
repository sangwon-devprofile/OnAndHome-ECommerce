package com.onandhome.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 로컬 디스크에 파일을 저장하는 서비스
 */
@Service
public class FileStorageService {

    private final Path uploadPath;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        // application.properties의 file.upload-dir=uploads 사용
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadPath); // 폴더가 없으면 생성
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉터리를 생성할 수 없습니다: " + this.uploadPath, e);
        }
    }

    /**
     * 파일을 저장하고, 브라우저에서 접근 가능한 URL(/uploads/파일명)을 반환
     */
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 원본 파일명
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // 확장자 추출
        String ext = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < originalFilename.length() - 1) {
            ext = originalFilename.substring(dotIndex + 1);
        }

        // 충돌 방지를 위해 UUID 기반 새 파일명 생성
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String storedFilename = uuid + (ext.isEmpty() ? "" : "." + ext);

        try {
            Path targetLocation = this.uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + originalFilename, e);
        }

        // static-locations: file:uploads/ 로 매핑되어 있으므로
        // /uploads/파일명 으로 접근 가능
        return "/uploads/" + storedFilename;
    }
}
