package com.kh.finalproject.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.finalproject.dto.QuizDto;
import com.kh.finalproject.service.AdminService;
import com.kh.finalproject.service.QuizService;
import com.kh.finalproject.vo.QuizReportDetailVO;
import com.kh.finalproject.vo.QuizReportStatsVO;
import com.kh.finalproject.vo.TokenVO;

@CrossOrigin
@RestController
@RequestMapping("/admin")
public class AdminRestController {
	
	@Autowired
	private QuizService quizService;
	
	@Autowired
	private AdminService adminService;
	
	//퀴즈 신고 관리 페이지
	@GetMapping("/quizzes/reports")
	public List<QuizReportStatsVO> getReportList(
			@RequestParam String status,
			@RequestAttribute TokenVO tokenVO
			) {
		
		String loginLevel = tokenVO.getLoginLevel();
		
		return adminService.getReportedQuizList(loginLevel, status);
	}
	//퀴즈 신고 상세 내역 페이지
	@GetMapping("/quizzes/{quizId}/reports")
	public List<QuizReportDetailVO> getReportDetail(
			@PathVariable int quizId,
			@RequestAttribute TokenVO tokenVO
			) {
		
		String loginLevel = tokenVO.getLoginLevel();
		
        return adminService.getReportDetails(loginLevel, quizId);
    }
	
	//퀴즈 삭제
	@DeleteMapping("/quizzes/{quizId}")
    public boolean deleteQuiz(
    		@PathVariable long quizId,
            @RequestAttribute TokenVO tokenVO
            ) {
        String loginId = tokenVO.getLoginId();
        String loginLevel = tokenVO.getLoginLevel();
		
        return quizService.deleteQuiz(quizId, loginId, loginLevel);
    }
	
	//퀴즈 상태 변경
	@PatchMapping("/quizzes/{quizId}/status/{status}")
	public boolean changeStatus(
			@PathVariable long quizId,
			@PathVariable String status,
			@RequestAttribute TokenVO tokenVO
			) {
		
		String loginId = tokenVO.getLoginId();
		String loginLevel = tokenVO.getLoginLevel();
		
		//퀴즈 상태 변경
		QuizDto quizDto = QuizDto.builder()
					.quizId(quizId)
					.quizStatus(status)
				.build();
		
		return quizService.changeQuizStatus(quizDto, loginId, loginLevel);
	}
}






