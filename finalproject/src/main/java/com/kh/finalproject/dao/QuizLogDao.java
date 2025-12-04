package com.kh.finalproject.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.finalproject.dto.QuizLogDto;

@Repository
public class QuizLogDao {

	@Autowired
	private SqlSession sqlSession;
	
	//퀴즈 기록 등록 메소드
	public QuizLogDto insert(QuizLogDto quizLogDto) {
		long sequence = sqlSession.selectOne("quiz_log.sequence");
		quizLogDto.setQuizLogId(sequence);
		sqlSession.insert("quizLog.insert", quizLogDto);
		
		return sqlSession.selectOne("quizLog.detail", sequence);
	}
	
	//퀴즈 기록 상세 정보 조회
	public QuizLogDto selectOne(long quizLogId){
		return sqlSession.selectOne("quizLog.detail", quizLogId);
	}
	
	//마이페이지 전용 조회
	public List<QuizLogDto> selectListByMember(String quizLogMemberId){
		return sqlSession.selectList("quizLog.selectListByMember", quizLogMemberId);
	}
	
	//랭킹을 위한 조회
	public int countCorrectAnswer(String quizLogMemberId) {
		return sqlSession.selectOne("quizLog.countCorrectAnswer", quizLogMemberId);
	}
	
	//어떤 사람이 해당 문제를 풀었는지 조회(관리자용)
	public List<QuizLogDto> selectList(long quizLogQuizId){
		return sqlSession.selectList("quizLog.selectList", quizLogQuizId);
	}
}





