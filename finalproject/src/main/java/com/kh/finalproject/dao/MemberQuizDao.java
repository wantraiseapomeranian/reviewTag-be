package com.kh.finalproject.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.finalproject.dto.QuizDto;
import com.kh.finalproject.vo.MemberQuizListVO;
import com.kh.finalproject.vo.MemberQuizRateVO;

@Repository
public class MemberQuizDao {

	@Autowired
	private SqlSession sqlSession;
	
	//// 내가 등록한 퀴즈관련 항목
	public List<QuizDto> selectAddList(String loginId){
		return sqlSession.selectList("quiz.listByMyQuiz",loginId);
	}
	
	/// 내가 푼 퀴즈 관련 항목
	// 푼 퀴즈 리스트
	public List<MemberQuizListVO> selectAnswerList(String loginId){
		return sqlSession.selectList("memberQuiz.listByAnswerQuiz",loginId);
	}
	// 푼 퀴즈 정답률
	public List<MemberQuizRateVO> selectAnswerQuizRate(String loginId){
		return sqlSession.selectList("memberQuiz.listByAnswerQuizRate", loginId);
	}
	
	
	
}
