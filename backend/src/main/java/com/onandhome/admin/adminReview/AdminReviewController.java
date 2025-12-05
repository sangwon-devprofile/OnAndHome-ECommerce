package com.onandhome.admin.adminReview;

import com.onandhome.review.ReviewService;
import com.onandhome.review.ReviewReplyService;
import com.onandhome.review.dto.ReviewDTO;
import com.onandhome.review.dto.ReviewReplyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ 관리자 리뷰 컨트롤러
 * 리뷰 목록, 상세, 답글 등록/수정/삭제 관리
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/board/review")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final ReviewReplyService reviewReplyService;

    /** ✅ 리뷰 목록 (검색 기능 포함) */
    @GetMapping("/list")
    public String list(@RequestParam(value = "kw", required = false) String keyword, Model model) {
        List<ReviewDTO> reviews;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색 결과 반환
            reviews = reviewService.search(keyword);
            model.addAttribute("kw", keyword);
        } else {
            // 전체 목록 반환
            reviews = reviewService.findAll();
        }
        
        model.addAttribute("reviews", reviews);
        return "admin/board/review/list";
    }

    /** ✅ 리뷰 상세보기 */
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long reviewId, Model model) {
        ReviewDTO review = reviewService.findById(reviewId);
        List<ReviewReplyDTO> replies = reviewReplyService.findByReviewId(reviewId);

        model.addAttribute("review", review);
        model.addAttribute("replies", replies);
        return "admin/board/review/detail";
    }

    /** ✅ 답글 등록 */
    @PostMapping("/reply/{reviewId}")
    public String createReply(@PathVariable Long reviewId,
                              @RequestParam("content") String content) {
        reviewReplyService.createReply(reviewId, content, "관리자", "admin", 1L);
        return "redirect:/admin/board/review/detail/" + reviewId;
    }

    /** ✅ 답글 수정 폼 */
    @GetMapping("/reply/edit/{replyId}/{reviewId}")
    public String editReplyForm(@PathVariable Long replyId,
                                @PathVariable Long reviewId,
                                Model model) {
        ReviewReplyDTO reply = reviewReplyService.findById(replyId);
        ReviewDTO review = reviewService.findById(reviewId); // ✅ 리뷰 데이터도 같이 전달
        model.addAttribute("reply", reply);
        model.addAttribute("review", review);
        return "admin/board/review/reply-edit";
    }

    /** ✅ 답글 수정 저장 */
    @PostMapping("/reply/edit/{replyId}/{reviewId}")
    public String editReply(@PathVariable Long replyId,
                            @PathVariable Long reviewId,
                            @RequestParam("content") String content) {
        reviewReplyService.updateReply(replyId, content);
        return "redirect:/admin/board/review/detail/" + reviewId;
    }

    /** ✅ 답글 삭제 */
    @PostMapping("/reply/delete/{replyId}/{reviewId}")
    public String deleteReply(@PathVariable Long replyId,
                              @PathVariable Long reviewId) {
        reviewReplyService.deleteReply(replyId);
        return "redirect:/admin/board/review/detail/" + reviewId;
    }

    /** ✅ 리뷰 삭제 */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long reviewId) {
        reviewService.deleteById(reviewId);
        return "redirect:/admin/board/review/list";
    }
}
