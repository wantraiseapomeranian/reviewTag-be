package com.kh.finalproject.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.finalproject.dao.ReviewDao;
import com.kh.finalproject.dao.ReviewLikeDao;
import com.kh.finalproject.dto.ReviewDto;
import com.kh.finalproject.error.TargetNotfoundException;
import com.kh.finalproject.service.ReviewService;
import com.kh.finalproject.vo.ReviewLikeVO;

@CrossOrigin 
@RestController
@RequestMapping("/review")
public class ReviewRestController {

    private final ReviewService reviewService;
	@Autowired
	private ReviewDao reviewDao;
	@Autowired
	private ReviewLikeDao reviewLikeDao;

    ReviewRestController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

	// 등록
	@PostMapping("/")
	public void insert(@RequestBody ReviewDto reviewDto) {
		reviewService.addReview(reviewDto);
	}

	// 전체 리뷰 조회
	@GetMapping("/reviewContents/{reviewContents}")
	public List<ReviewDto> selectByContents(@PathVariable Long reviewContents) {
		return reviewDao.selectByContents(reviewContents);
	}


	
	//로그인 리뷰 조회
	@GetMapping("/user/{reviewContents}/{reviewWriter}")
	public ReviewDto selectByUserAndContents(@PathVariable String reviewWriter,
											@PathVariable Long reviewContents) {
		ReviewDto reviewDto = reviewDao.selectByUserAndContents(reviewWriter, reviewContents);
		return reviewDto;
	}

	// 단일 리뷰 조회
	@GetMapping("/{reviewContents}/{reviewNo}")
	public ReviewDto selectOne(
			@PathVariable Long reviewContents,
			@PathVariable Long reviewNo) {
		return reviewDao.selectOne(reviewContents,reviewNo);
	}

	// 컨텐츠 아이디로 조회
	@GetMapping("/list/{contentsId}")
	public List<ReviewDto> selectById(@PathVariable Long contentsId) {
		List<ReviewDto> reviewList = reviewDao.selectListByContentsId(contentsId);
		if (reviewList == null || reviewList.isEmpty())
			throw new TargetNotfoundException();
		return reviewList;
	}

	// 수정
	@PatchMapping("/{reviewContents}/{reviewNo}")
	public void updateUnit(@RequestBody ReviewDto reviewDto, 
			@PathVariable Long reviewContents,
			@PathVariable Long reviewNo) {

		reviewDto.setReviewContents(reviewContents);
		reviewDto.setReviewNo(reviewNo);

		boolean success = reviewDao.updateUnit(reviewDto);
		if (!success) {
			throw new TargetNotfoundException(); // 수정실패
		}
	}

	// 삭제
	@DeleteMapping("/{reviewContents}/{reviewNo}")
    public void delete(
        @PathVariable("reviewContents") Long reviewContents,
        @PathVariable("reviewNo") Long reviewNo
    ) {
        System.out.println("삭제 메서드 진입");

        ReviewDto originDto = reviewDao.selectOne(reviewContents, reviewNo);
        if(originDto == null) throw new TargetNotfoundException();

        boolean success = reviewDao.delete(reviewContents, reviewNo);
        if(!success) throw new TargetNotfoundException();
    }



	// 좋아요 관련

	@PostMapping("/check")
	public ReviewLikeVO check(@RequestParam String loginId, @RequestParam Long reviewNo) {
		boolean like = reviewLikeDao.check(loginId, reviewNo);
		Long count = reviewLikeDao.countByReviewNo(reviewNo);
		return ReviewLikeVO.builder().like(like).count(count).build();
	}

	@PostMapping("/action/{reviewNo}/{loginId}")
	public ReviewLikeVO action(@PathVariable String loginId, @PathVariable Long reviewNo) {
		boolean before = reviewLikeDao.check(loginId, reviewNo);
		if (before) {// 좋아요 한 상태면
			reviewLikeDao.delete(loginId, reviewNo);
		} else {// 좋아요 안한 상태면
			reviewLikeDao.insert(loginId, reviewNo);
		}

		Long count = reviewLikeDao.countByReviewNo(reviewNo);

		reviewDao.updateReviewReviewLike(reviewNo, count);
		return ReviewLikeVO.builder().like(!before).count(count).build();
	}

}
