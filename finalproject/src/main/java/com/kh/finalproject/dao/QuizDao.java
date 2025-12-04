package com.kh.finalproject.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.finalproject.dto.QuizDto;

@Repository
public class QuizDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	//퀴즈 등록
	public QuizDto insert(QuizDto quizDto) {
		long sequence = sqlSession.selectOne("quiz.sequence");
		quizDto.setQuizId(sequence);
		sqlSession.insert("quiz.insert", quizDto);
		
		return sqlSession.selectOne("quiz.detail", sequence);
	}
	
	//퀴즈 풀기 전용 조회 구문
	public List<QuizDto> selectRandomQuizList(long quizContentsId, String quizLogMemberId) {
		Map<String, Object> params = new HashMap<>();
		params.put("quizContentsId", quizContentsId);
		params.put("quizLogMemberId", quizLogMemberId);
		
		return sqlSession.selectList("quiz.selectRandomQuizList", params);
	}
	
	//퀴즈 상세 정보 조회 구문
	public QuizDto selectOne(long quizId) {
		return sqlSession.selectOne("quiz.detail", quizId);
	}
	
	//해당 영화의 퀴즈 목록 조회 구문
	public List<QuizDto> selectByContent(long quizContentsId){
		return sqlSession.selectList("quiz.selectByContent", quizContentsId);
	}
	
	//퀴즈 수정
	public boolean update(QuizDto quizDto) {
		return sqlSession.update("quiz.update", quizDto) > 0;
	}
	
	//신고 누적 횟수 변경
	public boolean updateQuizReportCount(QuizDto quizDto) {
		return sqlSession.update("quiz.updateQuizReportCount", quizDto) > 0;
	}
	
	//관리자 상태 변경 메소드(이게 삭제 메소드 역할까지 함)
	public boolean updateQuizStatus(QuizDto quizDto) {
		return sqlSession.update("quiz.updateQuizStatus", quizDto) > 0;
	}
}





