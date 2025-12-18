package com.kh.finalproject.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.finalproject.configuration.DailyQuestProperties;
import com.kh.finalproject.dao.DailyQuizDao; // [NEW] DAO 추가
import com.kh.finalproject.dao.PointGetQuestDao;
import com.kh.finalproject.vo.DailyQuestVO;
import com.kh.finalproject.vo.DailyQuizVO;

@Service
public class DailyQuestService {

    @Autowired private DailyQuestProperties questProps; 
    @Autowired private PointGetQuestDao questDao;       // 퀘스트 로그 관리
    
    // [변경 1] MemberDao 제거 -> PointService, DailyQuizDao 추가
    @Lazy
    @Autowired private PointService pointService;       // 포인트 지급 및 이력 관리
    @Autowired private DailyQuizDao quizDao;            // 퀴즈 DB 접근 (SqlSession 사용)

    // [변경 2] 기존의 static List<DailyQuizVO> 및 static 블록 전체 삭제함.
    // (이제 데이터는 오라클 DB에서 가져옵니다)

    private String getTodayStr() {
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    // 1. 퀘스트 목록 조회 (기존 로직 유지)
    public List<DailyQuestVO> getQuestList(String memberId) {
        String today = getTodayStr();
        List<Map<String, Object>> logs = questDao.selectTodayLogs(memberId, today);
        
        Map<String, Map<String, Object>> logMap = logs.stream()
            .collect(Collectors.toMap(m -> (String) m.get("type"), m -> m));

        List<DailyQuestVO> result = new ArrayList<>();

        for (DailyQuestProperties.QuestDetail q : questProps.getList()) {
            Map<String, Object> log = logMap.get(q.getType());
            int current = (log != null) ? Integer.parseInt(String.valueOf(log.get("count"))) : 0;
            boolean claimed = (log != null) && "Y".equals(log.get("rewardYn"));
            boolean done = current >= q.getTarget();

            result.add(DailyQuestVO.builder()
                    .type(q.getType()).title(q.getTitle()).current(current).target(q.getTarget())
                    .reward(q.getReward()).done(done).claimed(claimed)
                    .desc(getDescByType(q.getType())).icon(getIconByType(q.getType())).action(getActionByType(q.getType()))
                    .build());
        }
        return result;
    }

    // [변경 3] 랜덤 퀴즈 추출 (DB 연동 + 중복 방지)
    public DailyQuizVO getRandomQuiz(String memberId) {
        // (1) 오늘 이미 퀴즈 퀘스트를 완료했는지 확인 (기존 로그 활용)
        List<Map<String, Object>> logs = questDao.selectTodayLogs(memberId, getTodayStr());
        boolean alreadySolved = logs.stream().anyMatch(m -> "QUIZ".equals(m.get("type")));

        // (2) 이미 풀었다면 null 반환 (프론트에서 '내일 다시 도전하세요' 처리)
        if (alreadySolved) {
            return null; 
        }

        // (3) 안 풀었다면 DAO를 통해 DB에서 랜덤 문제 1개 가져오기
        return quizDao.getRandomQuiz();
    }

    // [변경 4] 정답 검증 (보안 강화: 정답을 DB에서 직접 조회)
    // 파라미터 변경: correctAnswer(정답 문자열) 대신 quizNo(문제 번호)를 받습니다.
    @Transactional
    public boolean checkQuizAndProgress(String memberId, int quizNo, String userAnswer) {
        if (userAnswer == null) return false;

        // (1) DB에서 해당 문제의 '진짜 정답' 가져오기
        String correctAnswer = quizDao.getAnswer(quizNo); 
        
        if (correctAnswer == null) return false;

        // (2) 정답 비교 (공백 제거, 소문자 변환 등 유연하게 처리)
        String cleanUser = userAnswer.replace(" ", "").toLowerCase();
        String cleanCorrect = correctAnswer.replace(" ", "").toLowerCase();

        if (cleanUser.contains(cleanCorrect)) {
            // (3) 정답이면 퀘스트 진행도 상승 (이제 '완료' 상태가 됨)
            this.questProgress(memberId, "QUIZ");
            return true;
        }
        
        return false;
    }

    // 4. 퀘스트 진행도 상승 (공용)
    @Transactional
    public void questProgress(String memberId, String type) {
        boolean isValid = questProps.getList().stream().anyMatch(q -> q.getType().equals(type));
        if(isValid) {
            questDao.upsertQuestLog(memberId, type, getTodayStr());
        }
    }

    // [변경 5] 보상 수령 (PointService 적용)
    @Transactional
    public int claimReward(String memberId, String type) {
        DailyQuestProperties.QuestDetail targetQuest = questProps.getList().stream()
                .filter(q -> q.getType().equals(type)).findFirst()
                .orElseThrow(() -> new RuntimeException("존재하지 않는 퀘스트입니다."));

        List<Map<String, Object>> logs = questDao.selectTodayLogs(memberId, getTodayStr());
        Map<String, Object> myLog = logs.stream().filter(m -> m.get("type").equals(type)).findFirst().orElse(null);

        if (myLog == null) throw new RuntimeException("기록 없음");
        int current = Integer.parseInt(String.valueOf(myLog.get("count")));
        if (current < targetQuest.getTarget()) throw new RuntimeException("목표 미달성");
        if ("Y".equals(myLog.get("rewardYn"))) throw new RuntimeException("이미 수령");

        // 보상 수령 상태 업데이트
        if (questDao.updateRewardStatus(memberId, type, getTodayStr()) > 0) {
            
            // [핵심] PointService를 통해 포인트 지급 및 'GET' 이력 저장
            pointService.addPoint(
                memberId, 
                targetQuest.getReward(), 
                "GET"
            );
            
            return targetQuest.getReward();
        }
        return 0;
    }

    // --- [Helper 메소드] ---
    private String getIconByType(String type) {
        switch(type) {
            case "REVIEW": return "✍️"; case "QUIZ": return "🧠";
            case "LIKE": return "❤️"; case "ROULETTE": return "🎰"; default: return "❓";
        }
    }
    private String getDescByType(String type) {
        switch(type) {
            case "REVIEW": return "한줄평 남기기"; case "QUIZ": return "오늘의 영화 퀴즈";
            case "LIKE": return "좋아요 누르기"; case "ROULETTE": return "룰렛 돌리기"; default: return "일일 퀘스트";
        }
    }
    private String getActionByType(String type) {
        switch(type) {
            case "REVIEW": return "link"; case "QUIZ": return "quiz";
            case "LIKE": return "link"; case "ROULETTE": return "roulette"; default: return "none";
        }
    }
}