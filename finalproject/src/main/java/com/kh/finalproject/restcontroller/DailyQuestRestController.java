package com.kh.finalproject.restcontroller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.kh.finalproject.service.DailyQuestService;
import com.kh.finalproject.vo.DailyQuestVO;
import com.kh.finalproject.vo.DailyQuizVO;

@RestController
@RequestMapping("/point/quest")
@CrossOrigin
public class DailyQuestRestController {

    @Autowired private DailyQuestService dailyQuestService;

    // 1. 퀘스트 목록 조회 (기존 유지)
    @GetMapping("/list")
    public List<DailyQuestVO> list(@RequestAttribute(value="loginId", required=false) String loginId) {
        if(loginId == null) return List.of();
        return dailyQuestService.getQuestList(loginId);
    }

    // 2. [추가] 랜덤 퀴즈 문제 요청
    @GetMapping("/quiz/random")
    public DailyQuizVO getRandomQuiz() {
        return dailyQuestService.getRandomQuiz();
    }

    // 3. [추가] 퀴즈 정답 확인 및 진행도 업데이트
    @PostMapping("/quiz/check")
    public String checkQuiz(@RequestAttribute("loginId") String loginId, @RequestBody Map<String, String> body) {
        String answer = body.get("answer");
        String correctAnswer = body.get("correctAnswer");
        
        // 서비스에서 정답 확인 후 맞으면 DB에 count를 올림
        boolean isCorrect = dailyQuestService.checkQuizAndProgress(loginId, answer, correctAnswer);
        return isCorrect ? "success" : "fail";
    }

    // 4. 보상 받기 (기존 유지)
    @PostMapping("/claim")
    public String claim(@RequestAttribute("loginId") String loginId, @RequestBody Map<String, String> body) {
        try {
            String type = body.get("type");
            int reward = dailyQuestService.claimReward(loginId, type);
            return "success:" + reward;
        } catch (Exception e) {
            return "fail:" + e.getMessage();
        }
    }
}