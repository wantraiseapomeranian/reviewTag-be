package com.kh.finalproject.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.finalproject.dao.QuizReportDao;
import com.kh.finalproject.error.NeedPermissionException;
import com.kh.finalproject.vo.QuizReportDetailVO;
import com.kh.finalproject.vo.QuizReportStatsVO;

@Service
public class AdminService {
	
	@Autowired
	private QuizReportDao quizReportDao;
	
	//신고된 퀴즈 목록 조회
	public List<QuizReportStatsVO> getReportedQuizList(String loginLevel, String status){
		
		//관리자인지 검사
		if(loginLevel == null || !"관리자".equals(loginLevel))
			throw new NeedPermissionException();
		
		return quizReportDao.selectReportedQuizList(status);
	}
	
	//신고 상세 내역 조회(신고 종류가 기타일때만)
	public List<QuizReportDetailVO> getReportDetails(String loginLevel, int quizId){
		
		//관리자인지 검사
		if(loginLevel == null || !"관리자".equals(loginLevel))
			throw new NeedPermissionException();
				
		return quizReportDao.selectReportDetails(quizId);
	}
}





