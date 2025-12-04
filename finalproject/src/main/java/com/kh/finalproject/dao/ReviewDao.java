package com.kh.finalproject.dao;

import java.util.List;

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
	
	public ReviewDto selectOne(String contentsTitle) { //영화 제목으로 조회
		return sqlSession.selectOne("review.selectOne", contentsTitle);
	}
	
	//부분수정
	public boolean updateUnit(ReviewDto reviewDto) {
		return sqlSession.update("review.updateUnit", reviewDto) > 0;
	}
	
	//삭제
	public boolean delete(int reviewNo) {
		return sqlSession.delete("review.delete", reviewNo) > 0;
	}
}
