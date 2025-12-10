package com.kh.finalproject.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.finalproject.dao.ReviewDao;
import com.kh.finalproject.dao.ReviewLikeDao;
import com.kh.finalproject.dto.ReviewDto;
import com.kh.finalproject.error.TargetNotfoundException;
import com.kh.finalproject.vo.ReviewLikeVO;


@CrossOrigin
@RestController
@RequestMapping("/review")
public class ReviewRestController {
	@Autowired
	private ReviewDao reviewDao;
	@Autowired
	private ReviewLikeDao reviewLikeDao;
	
	//등록
	@PostMapping("/insert")
	public void insert(@RequestBody ReviewDto reviewDto) {
		reviewDao.insert(reviewDto);
	}
	
	//조회
	@GetMapping("/search")
	public List<ReviewDto> search() {
		return reviewDao.selectList();
	}

	//영화 제목으로 조회
	@GetMapping("/{contentsTitle}")
	public List<ReviewDto> selectByTitle(@PathVariable String contentsTitle) {
		List<ReviewDto> reviewList = reviewDao.detail(contentsTitle);
		if(reviewList == null || reviewList.isEmpty()) throw new TargetNotfoundException();
		return reviewList;
	}
	
	//컨텐츠 아이디로 조회
	@GetMapping("/list/{contentsId}")
	public List<ReviewDto> selectById(@PathVariable Long contentsId) {
		List<ReviewDto> reviewList = reviewDao.selectListByContentsId(contentsId);
		if(reviewList == null || reviewList.isEmpty()) throw new TargetNotfoundException();
		return reviewList;
	}
	
	//수정
	@PatchMapping("/{reviewNo}")
	public void updateUnit(@RequestBody ReviewDto reviewDto,
								@PathVariable Long reviewNo) {
		ReviewDto originDto = reviewDao.selectOne(reviewNo);
		if(originDto == null) throw new TargetNotfoundException();
		
		reviewDto.setReviewNo(reviewNo);
		
		boolean success = reviewDao.updateUnit(reviewDto);
		if(!success) {
			throw new TargetNotfoundException(); //수정실패
		}
	}

	//삭제
	@DeleteMapping("/{reviewNo}")
	public void delete(@PathVariable Long reviewNo) {
		
		boolean success = reviewDao.delete(reviewNo);
		if(!success) {
			throw new TargetNotfoundException(); //삭제실패
		}
	}
	
	
	//좋아요 관련
	
	
	@PostMapping("/check/{reviewNo}/{loginId}")
	public ReviewLikeVO check(@PathVariable String loginId, @PathVariable Long reviewNo) {
		boolean like = reviewLikeDao.check(loginId, reviewNo);
		Long count = reviewLikeDao.countByReviewNo(reviewNo);
		return ReviewLikeVO.builder().like(like).count(count).build();
	}
	
	@PostMapping("/action/{reviewNo}/{loginId}")
	public ReviewLikeVO action(@PathVariable String loginId, @PathVariable Long reviewNo) {
		boolean before = reviewLikeDao.check(loginId, reviewNo);
		if(before) {//좋아요 한 상태면
			reviewLikeDao.delete(loginId, reviewNo);
		}
		else {//좋아요 안한 상태면
			reviewLikeDao.insert(loginId, reviewNo);
		}
		
		Long count = reviewLikeDao.countByReviewNo(reviewNo);
		
		reviewDao.updateReviewReviewLike(reviewNo, count);
		return ReviewLikeVO.builder()
						.like(!before).count(count)
					.build();
	}
	
	
	
	
	
	
	
	
	
}
