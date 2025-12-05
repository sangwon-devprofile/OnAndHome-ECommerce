package com.onandhome.admin.adminProduct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;

    /**
     * 이미지 파일 업로드
     */
    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("이미지 업로드 요청: {}", file.getOriginalFilename());

            // 파일 타입 검증 (이미지만 허용)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 파일 크기 검증 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "파일 크기는 5MB를 초과할 수 없습니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // 파일 업로드
            String fileUrl = fileUploadService.uploadFile(file);

            response.put("success", true);
            response.put("message", "파일 업로드 성공");
            response.put("fileUrl", fileUrl);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("이미지 업로드 실패: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            log.error("이미지 업로드 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 파일 삭제
     */
    @DeleteMapping("/image")
    public ResponseEntity<Map<String, Object>> deleteImage(@RequestParam("fileUrl") String fileUrl) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("이미지 삭제 요청: {}", fileUrl);

            fileUploadService.deleteFile(fileUrl);

            response.put("success", true);
            response.put("message", "파일 삭제 성공");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("이미지 삭제 중 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "파일 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
