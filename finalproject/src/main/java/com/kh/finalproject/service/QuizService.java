package com.kh.finalproject.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.finalproject.dao.QuizDao;
import com.kh.finalproject.dto.QuizDto;

@Service
public class QuizService {
	
	@Autowired
	private QuizDao quizDao;
	
	//퀴즈 등록
	//추후 퀴즈 등록 시 포인트 지급이 필요한 경우 추가 로직 작성이 필요함
	@Transactional
	public QuizDto registQuiz(QuizDto quizDto) {
		
        return quizDao.insert(quizDto);
    }
	
	//랜덤 5문제 출제
	//추후 quizLogService에 트랜잭션 처리 예정
	public List<QuizDto> getQuizGame(long contentsId, String memberId) {
        return quizDao.selectRandomQuizList(contentsId, memberId);
    }
	
	//퀴즈 삭제 (상태만 변경)
	//추후 통계를 위해 delete 하는것 보다는 상태만 변경하여 숨김 처리 구현 예정
    public boolean deleteQuiz(long quizId) {
    	QuizDto quizDto = QuizDto.builder()
    	        	.quizId(quizId)
    	        	.quizStatus("DELETED")
    	        .build();
        
        return quizDao.updateQuizStatus(quizDto);
    }
    
    //퀴즈 수정
    //Token으로 작성자 비교 로직 추가 예정
    public boolean editQuiz(QuizDto quizDto) {
        return quizDao.update(quizDto);
    }
    
    //관리자용 목록 조회
    // 추후에 관리자인지 아닌지 검사하는 로직 추가 예정
    public List<QuizDto> getQuizList(long contentsId) {
        return quizDao.selectByContent(contentsId);
    }
    
    //상세 조회
    //추후 마이페이지에 들어갈 경우 Token 비교 로직 추가 예정
    public QuizDto getQuizDetail(long quizId) {
        return quizDao.selectOne(quizId);
    }
    
    //상태 변경 메소드(관리자용)
    public boolean changeQuizStatus(QuizDto quizDto) {

        return quizDao.updateQuizStatus(quizDto);
    }
    
    //퀴즈 누적 신고 누적 횟수 변경
    public boolean reportQuiz(long quizId) {
        return quizDao.updateQuizReportCount(quizId);
    }
    
}
