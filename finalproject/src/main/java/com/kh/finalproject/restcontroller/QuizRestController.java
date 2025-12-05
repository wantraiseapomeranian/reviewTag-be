package com.kh.finalproject.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.finalproject.dto.QuizDto;
import com.kh.finalproject.error.NeedPermissionException;
import com.kh.finalproject.error.TargetNotfoundException;
import com.kh.finalproject.service.QuizService;
import com.kh.finalproject.vo.TokenVO;

@CrossOrigin
@RestController
@RequestMapping("/quiz")
public class QuizRestController {
	
	@Autowired
	private QuizService quizService;
	
	//퀴즈 등록
	//TokenVO로 사용자 조회 및 quiz_creator_id로 등록
	@PostMapping("/")
	public QuizDto insert(
			@RequestBody QuizDto quizDto,
			@RequestAttribute TokenVO tokenVO
			) {
		//토큰에서 memberId 추출 한 후 작성자 설정
		String memberId = tokenVO.getLoginId();
		quizDto.setQuizCreatorId(memberId);
		
		return quizService.registQuiz(quizDto);
	}
	
	//퀴즈 푸는 페이지
	//TokenVO로 사용자 조회
	@GetMapping("/game/{contentsId}")
	public List<QuizDto> game(
			@RequestAttribute TokenVO tokenVO,
			@PathVariable long contentsId
			){
		
		//비회원은 나가라만 설정해도 되나?
		if(tokenVO == null) throw new TargetNotfoundException();
		
		
		return quizService.getQuizGame(contentsId, "TokenVO.getLoginId()");
	}
	
	//해당 영화의 퀴즈 목록 조회 구문
	//관리자 페이지로 뺄 가능성 높음
	@GetMapping("/list/{contentsId}")
	public List<QuizDto> getList(
			@RequestAttribute TokenVO tokenVO,
			@PathVariable long contentsId
			) {
		
		//관리자 권한 체크 로직
		boolean isAdmin = tokenVO.getLoginLevel().equals("관리자");
		if(isAdmin == false) throw new NeedPermissionException();
		
		return quizService.getQuizList(contentsId);
	}
	
	//퀴즈 상세정보 조회
	@GetMapping("/{quizId}")
	public QuizDto detail(@PathVariable long quizId) {
		
		//quizId로 조회한 퀴즈가 없다면 나가는 코드 작성?
		QuizDto findDto = quizService.getQuizDetail(quizId);
		if(findDto == null) throw new TargetNotfoundException();
		
		return quizService.getQuizDetail(quizId);
	}
	
	//퀴즈 수정
	//TokenVO로 사용자 조회
	@PatchMapping("/")
	public boolean update(
			@RequestAttribute TokenVO tokenVO,
			@RequestBody QuizDto quizDto) {
		
		//로그인 한 계정과 작성자의 아이디가 같은지 검사
		String loginId = tokenVO.getLoginId();
		String creatorId = quizDto.getQuizCreatorId();
		
		boolean isCorrect = loginId.equals(creatorId);
		if(isCorrect) throw new NeedPermissionException();
		
		return quizService.editQuiz(quizDto);
	}
	
	//퀴즈 상태 변경(BLIND)
	@PatchMapping("/status")
	public boolean changeStatus(
			@RequestAttribute TokenVO tokenVO,
			@RequestBody QuizDto quizDto) {
		
		// quizDto에는 quizId와 quizStatus("BLIND")가 들어있어야 함
		String loginId = tokenVO.getLoginId();
		String loginLevel = tokenVO.getLoginLevel();
		
		//작성자 or 관리자인지 검사
		boolean isOwner = loginId.equals(quizDto.getQuizCreatorId());
		boolean isAdmin = loginLevel.equals("관리자");
		
		if(isOwner || isAdmin) throw new NeedPermissionException();
		
		return quizService.changeQuizStatus(quizDto);
	}
	
	//신고 누적 횟수 변경
	//중복 클릭 불가 처리 예정
	@PatchMapping("/report/{quizId}")
	public boolean reportQuiz(@PathVariable long quizId) {
	    return quizService.reportQuiz(quizId);
	}
	
	//퀴즈 삭제
	@DeleteMapping("/{quizId}")
	public boolean delete(
			@RequestAttribute TokenVO tokenVO,
			@PathVariable long quizId) {
		
		//TokenVO에서 loginId와 loginLevel 추출해서
		String loginId = tokenVO.getLoginId();
		String loginLevel = tokenVO.getLoginLevel();
		
		//퀴즈가 있는지 검사
		QuizDto findDto = quizService.getQuizDetail(quizId);
		
		//작성자 or 관리자인지 검사
		boolean isOwner = loginId.equals(findDto.getQuizCreatorId());
		boolean isAdmin = loginLevel.equals("관리자");
		
		if(isOwner || isAdmin) throw new NeedPermissionException();
		
		return quizService.deleteQuiz(quizId);
	}
	
	
}





