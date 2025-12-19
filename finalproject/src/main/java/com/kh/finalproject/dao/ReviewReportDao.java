package com.kh.finalproject.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.finalproject.dto.ReviewReportDto;
import com.kh.finalproject.vo.ReviewReportListVO;

@Repository
public class ReviewReportDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	
	//리뷰 신고 등록
	public void insertReviewReport(ReviewReportDto reviewReportDto) {
		sqlSession.insert("reviewReport.insertReviewReport", reviewReportDto);
	}
	
	//리뷰 신고 조회(마이페이지 and 관리자 페이지)
	public List<ReviewReportListVO> selectList(){
		return sqlSession.selectList("reviewReport.selectList");
	}
	
	
	//리뷰 신고 삭제(관리자 페이지)
}
