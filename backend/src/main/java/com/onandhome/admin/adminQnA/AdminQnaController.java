package com.onandhome.admin.adminQnA;

import com.onandhome.qna.entity.Qna;
import com.onandhome.qna.entity.QnaReply;
import com.onandhome.qna.QnaService;
import com.onandhome.qna.QnaReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Q&A 관리 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/board/qna")
public class AdminQnaController {

    private final QnaService qnaService;
    private final QnaReplyService qnaReplyService;

    /** ✅ QnA 목록 페이지 (검색 기능 포함) */
    @GetMapping("/list")
    public String list(@RequestParam(value = "kw", required = false) String keyword, Model model) {
        List<Qna> qnaList;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색 결과 반환
            qnaList = qnaService.search(keyword);
            model.addAttribute("kw", keyword);
        } else {
            // 전체 목록 반환
            qnaList = qnaService.findAll();
        }
        
        model.addAttribute("qnaList", qnaList);
        return "admin/board/qna/list";
    }

    /** ✅ QnA 작성 폼 */
    @GetMapping("/write")
    public String writeView(Model model) {
        model.addAttribute("qna", new Qna());
        return "admin/board/qna/write";
    }

    /** ✅ QnA 작성 저장 */
    @PostMapping("/write")
    public String write(@ModelAttribute Qna qna) {
        qnaService.save(qna);
        return "redirect:/admin/board/qna/list";
    }

    /** ✅ QnA 상세 보기 */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Qna qna = qnaService.findById(id);
        if (qna == null) throw new IllegalArgumentException("Q&A 없음");

        // ✅ 이 질문에 달린 모든 리플라이 조회
        List<QnaReply> replies = qnaReplyService.findByQnaId(id);

        model.addAttribute("qna", qna);
        model.addAttribute("replies", replies);
        return "admin/board/qna/detail";
    }

    /** ✅ QnA 수정 폼 */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Qna qna = qnaService.findById(id);
        model.addAttribute("qna", qna);
        return "admin/board/qna/edit";
    }

    /** ✅ QnA 수정 저장 */
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute Qna qna) {
        qnaService.update(id, qna);
        return "redirect:/admin/board/qna/list";
    }

    /** ✅ QnA 삭제 */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        qnaService.delete(id);
        return "redirect:/admin/board/qna/list";
    }

    /** ✅ 리플라이 등록 */
    @PostMapping("/reply/{id}")
    public String addReply(@PathVariable Long id,
                           @RequestParam("content") String content) {
        qnaReplyService.createReply(id, content, "관리자");
        return "redirect:/admin/board/qna/" + id;
    }

    /** ✅ 리플라이 수정 폼 */
    @GetMapping("/reply/edit/{replyId}")
    public String editReplyForm(@PathVariable Long replyId, Model model) {
        QnaReply reply = qnaReplyService.findById(replyId);
        if (reply == null) throw new IllegalArgumentException("답변 없음");

        model.addAttribute("reply", reply);
        model.addAttribute("qnaId", reply.getQna().getId());
        return "admin/board/qna/reply-edit";
    }

    /** ✅ 리플라이 수정 저장 */
    @PostMapping("/reply/edit/{replyId}")
    public String editReply(@PathVariable Long replyId,
                            @RequestParam("content") String content) {
        qnaReplyService.updateReply(replyId, content);
        Long qnaId = qnaReplyService.findById(replyId).getQna().getId();
        return "redirect:/admin/board/qna/" + qnaId;
    }

    /** ✅ 리플라이 삭제 */
    @PostMapping("/reply/delete/{replyId}")
    public String deleteReply(@PathVariable Long replyId,
                              @RequestParam("qnaId") Long qnaId) {
        qnaReplyService.deleteReply(replyId);
        return "redirect:/admin/board/qna/" + qnaId;
    }
}
