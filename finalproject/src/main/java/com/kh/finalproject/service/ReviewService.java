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
		reviewDao.insert(reviewDto);
		memberDao.updateReliability(reviewDto.getReviewWriter(), 1);
	}
	
	public void minusReview(Long reviewContents, Long reviewNo) {
		reviewDao.delete(reviewContents, reviewNo);
//		memberDao.deleteReliability(reviewContents, 0);
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
