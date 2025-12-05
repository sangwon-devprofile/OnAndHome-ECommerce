package com.onandhome.admin.adminNotice;

import com.onandhome.Notice.NoticeService;
import com.onandhome.Notice.dto.NoticeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ 관리자 공지사항 컨트롤러 (DTO 반환 방식)
 * 경로 기준: com.onandhome.admin.adminNotice
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/board/notice")
public class AdminNoticeController {

    private final NoticeService noticeService;

    /** ✅ 공지사항 목록 (검색 기능 포함) */
    @GetMapping("/list")
    public String list(@RequestParam(value = "kw", required = false) String keyword, Model model) {
        List<NoticeDto> noticeList;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색 결과 반환
            noticeList = noticeService.search(keyword);
            model.addAttribute("kw", keyword);
        } else {
            // 전체 목록 반환
            noticeList = noticeService.findAll();
        }
        
        model.addAttribute("noticeList", noticeList);
        return "admin/board/notice/list";
    }

    /** ✅ 공지사항 상세보기 */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        NoticeDto notice = noticeService.findById(id);
        model.addAttribute("notice", notice);
        return "admin/board/notice/detail";
    }

    /** ✅ 공지사항 작성 폼 */
    @GetMapping("/write")
    public String writeView(Model model) {
        model.addAttribute("notice", new NoticeDto());
        return "admin/board/notice/write";
    }

    /** ✅ 공지사항 등록 처리 */
    @PostMapping("/write")
    public String write(@ModelAttribute NoticeDto dto) {
        log.info("📨 등록 요청 도착 - title={}, writer={}, content={}",
                dto.getTitle(), dto.getWriter(), dto.getContent());

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            log.error("❌ 제목이 비어 있습니다. 등록 중단");
            throw new IllegalArgumentException("제목은 필수 입력 항목입니다.");
        }

        noticeService.createNotice(dto);
        log.info("✅ 공지사항 등록 완료");
        return "redirect:/admin/board/notice/list";
    }

    /** ✅ 공지사항 수정 폼 */
    @GetMapping("/edit/{id}")
    public String editView(@PathVariable Long id, Model model) {
        NoticeDto notice = noticeService.findById(id);
        model.addAttribute("notice", notice);
        return "admin/board/notice/edit";
    }

    /** ✅ 공지사항 수정 처리 (수정 후 목록으로 이동) */
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute NoticeDto dto) {
        log.info("✏️ 수정 요청 도착 - id={}, title={}", id, dto.getTitle());
        noticeService.update(id, dto);
        log.info("✅ 수정 완료 → 목록으로 이동");
        return "redirect:/admin/board/notice/list";
    }

    /** ✅ 공지사항 삭제 */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        noticeService.delete(id);
        return "redirect:/admin/board/notice/list";
    }
}
