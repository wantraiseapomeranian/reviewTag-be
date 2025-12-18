package com.kh.finalproject.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.finalproject.dao.MemberDao;
import com.kh.finalproject.dao.ReviewDao;
import com.kh.finalproject.dao.ReviewLikeDao;
import com.kh.finalproject.dto.ReviewDto;
import com.kh.finalproject.error.TargetNotfoundException;

@Service
public class ReviewService {

	@Autowired
	private ReviewDao reviewDao;
	@Autowired
	private MemberDao memberDao;
	@Autowired
	private ReviewLikeDao reviewLikeDao;

	@Transactional
    public void addReview(ReviewDto reviewDto) {
        // 1. 중복 체크 (이미 쓴 리뷰가 있는지 확인)
        // 기존에 있는 selectByUserAndContents 메소드를 활용합니다.
        ReviewDto findDto = reviewDao.selectByUserAndContents(
                reviewDto.getReviewWriter(), 
                reviewDto.getReviewContents()
        );

        if (findDto != null) {
            // 이미 리뷰가 존재하면 에러를 발생시켜 중단 (또는 return으로 조용히 종료)
            throw new IllegalStateException("이미 이 콘텐츠에 리뷰를 작성하셨습니다.");
        }

        // 2. 리뷰 등록 (DAO 호출은 여기서만!)
        reviewDao.insert(reviewDto);

        // 3. 작성자 신뢰도 +1
        String writer = reviewDto.getReviewWriter();
        memberDao.updateReliability(writer, 1);
    }

	// 리뷰 삭제시 신뢰도 -1
	public void deleteReview(Long reviewContents, Long reviewNo) {
		
	    ReviewDto review = reviewDao.selectOne(reviewContents, reviewNo);
	    if (review == null) {
	        throw new TargetNotfoundException();
	    }
	    
	    String writer = review.getReviewWriter();
	    memberDao.updateReliability(writer, -1);
	}
	
	// 좋아요에 대한 신뢰도 갱신 (3좋아요 1신뢰도)
	public void LikeReviewRel(Long reviewNo) {
		String writer = reviewDao.findWriterByReviewNo(reviewNo);
		int totalLike = reviewLikeDao.countTotalLikeByWriter(writer);
		int reliability = totalLike/3;
		
		memberDao.updateReliabilitySet(writer, reliability);
	}


}
