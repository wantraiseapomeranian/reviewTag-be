package com.kh.finalproject.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.finalproject.dto.ReviewDto;

@Repository
public class ReviewDao {
	@Autowired
	private SqlSession sqlSession;
	
	//등록
	public void insert(ReviewDto reviewDto) {
		sqlSession.insert("review.insert", reviewDto);
	}
	
	//조회
	public List<ReviewDto> selectList() { //전체
		return sqlSession.selectList("review.selectList");
	}
	
	//contentsId로 list 조회
	public List<ReviewDto> selectListByContentsId (Long reviewContents) {
		return sqlSession.selectList("review.selectListByContentsId", reviewContents);
	}
	
	public ReviewDto selectOne(Long reviewNo) {
		return sqlSession.selectOne("review.selectOne", reviewNo);
	}
	
	public List<ReviewDto> detail(String contentsTitle) { //컨텐츠 제목으로 조회
		return sqlSession.selectList("review.detail", contentsTitle);
	}
	
	//부분수정
	public boolean updateUnit(ReviewDto reviewDto) {
		return sqlSession.update("review.updateUnit", reviewDto) > 0;
	}
	
	//삭제
	public boolean delete(Long reviewNo) {
		return sqlSession.delete("review.delete", reviewNo) > 0;
	}
	
	
	//////////////////////////////////////////
	
	//좋아요 관련
	public void updateReviewLike(Long reviewNo) {
		sqlSession.update("review.updateReviewLike", reviewNo);
	}
	
	public void updateReviewReviewLike(Long reviewNo, Long count) {
		Map<String, Object> params = new HashMap<>();
		params.put("reviewNo", reviewNo);
		params.put("count", count);
		sqlSession.update("review.updateReviewReviewLike", params);
	}

}
