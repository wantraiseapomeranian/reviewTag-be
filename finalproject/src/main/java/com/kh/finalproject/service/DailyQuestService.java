package com.kh.finalproject.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.finalproject.configuration.DailyQuestProperties;
import com.kh.finalproject.dao.MemberDao;
import com.kh.finalproject.dao.PointGetQuestDao;
import com.kh.finalproject.dto.MemberDto;
import com.kh.finalproject.vo.DailyQuestVO;
import com.kh.finalproject.vo.DailyQuizVO; // 명칭 변경 반영

@Service
public class DailyQuestService {

    @Autowired private DailyQuestProperties questProps; // yml 설정값
    @Autowired private PointGetQuestDao questDao;       // 로그 DB
    @Autowired private MemberDao memberDao;             // 포인트 지급용

    // --- [영화/애니메이션 퀴즈 데이터 50문항] ---
    private static final List<DailyQuizVO> QUIZ_LIST = new ArrayList<>();
    private static final Random RANDOM = new Random();

    static {
        // [한국 영화]
        QUIZ_LIST.add(new DailyQuizVO("영화 '기생충'으로 아카데미 감독상을 받은 인물은?", "봉준호"));
        QUIZ_LIST.add(new DailyQuizVO("영화 '명량'에서 이순신 장군 역을 맡은 배우는?", "최민식"));
        QUIZ_LIST.add(new DailyQuizVO("'어이가 없네?' 대사로 유명한 영화 '베테랑'의 배우는?", "유아인"));
        QUIZ_LIST.add(new DailyQuizVO("공유가 주연한 한국 최초의 좀비 블록버스터는?", "부산행"));
        QUIZ_LIST.add(new DailyQuizVO("마동석 주연의 형사 액션 시리즈 제목은?", "범죄도시"));
        QUIZ_LIST.add(new DailyQuizVO("영화 '올드보이' 오대수가 15년 동안 먹은 음식은?", "군만두"));
        QUIZ_LIST.add(new DailyQuizVO("영화 '관상'에서 '내가 왕이 될 상인가'라고 물은 인물은?", "이정재"));
        QUIZ_LIST.add(new DailyQuizVO("눈물 없인 볼 수 없는 7번방의 ○○. 빈칸은?", "선물"));
        QUIZ_LIST.add(new DailyQuizVO("영화 '신과함께' 시리즈의 원작 웹툰 작가는?", "주호민"));
        QUIZ_LIST.add(new DailyQuizVO("영화 '내부자들' 이병헌이 모히또 가서 마시자고 한 곳은?", "몰디브"));

        // [외국 영화/디즈니/마블]
        QUIZ_LIST.add(new DailyQuizVO("마블 영화 '아이언맨'의 본명은?", "토니 스타크"));
        QUIZ_LIST.add(new DailyQuizVO("'아이 엠 유어 파더' 대사가 나오는 우주 영화는?", "스타워즈"));
        QUIZ_LIST.add(new DailyQuizVO("마블 캐릭터 중 망치 '묠니르'를 사용하는 영웅은?", "토르"));
        QUIZ_LIST.add(new DailyQuizVO("디즈니 '겨울왕국'의 눈사람 캐릭터 이름은?", "올라프"));
        QUIZ_LIST.add(new DailyQuizVO("해리 포터가 다니는 마법 학교 이름은?", "호그와트"));
        QUIZ_LIST.add(new DailyQuizVO("'어벤져스' 보라색 피부의 최종 보스 이름은?", "타노스"));
        QUIZ_LIST.add(new DailyQuizVO("배트맨의 숙적인 광기 어린 악당의 이름은?", "조커"));
        QUIZ_LIST.add(new DailyQuizVO("영화 '타이타닉' 남주인공 배우의 성은 ○○○○○?", "디카프리오"));
        QUIZ_LIST.add(new DailyQuizVO("디즈니 '라이온 킹'의 주인공 사자 이름은?", "심바"));
        QUIZ_LIST.add(new DailyQuizVO("'도레미 송'으로 유명한 뮤지컬 영화는 '사운드 오브 ○○'?", "뮤직"));

        // [일본 애니메이션]
        QUIZ_LIST.add(new DailyQuizVO("'너의 이름은'을 제작한 감독의 이름은?", "신카이 마코토"));
        QUIZ_LIST.add(new DailyQuizVO("거대한 토끼 모양 정령이 나오는 지브리 만화는?", "토토로"));
        QUIZ_LIST.add(new DailyQuizVO("'귀멸의 칼날' 주인공 카마도 ○○○?", "탄지로"));
        QUIZ_LIST.add(new DailyQuizVO("'원피스' 루피가 먹은 악마의 열매는?", "고무고무"));
        QUIZ_LIST.add(new DailyQuizVO("'슬램덩크' 주인공 강백호의 등번호는?", "10번"));
        QUIZ_LIST.add(new DailyQuizVO("'센과 치히로의 행방불명' 속 얼굴 없는 요괴는?", "가오나시"));
        QUIZ_LIST.add(new DailyQuizVO("'드래곤볼'에서 소원을 들어주는 용은?", "신룡"));
        QUIZ_LIST.add(new DailyQuizVO("'포켓몬스터' 지우의 파트너 포켓몬은?", "피카츄"));
        QUIZ_LIST.add(new DailyQuizVO("'명탐정 코난' 고등학생 탐정 시절의 이름은?", "남도일"));
        QUIZ_LIST.add(new DailyQuizVO("'에반게리온'에 등장하는 거대 생체 병기 명칭은?", "에바"));

        // [픽사/드림웍스/애니]
        QUIZ_LIST.add(new DailyQuizVO("'토이 스토리' 보안관 인형의 이름은?", "우디"));
        QUIZ_LIST.add(new DailyQuizVO("'짱구는 못말려'에서 짱구가 가장 좋아하는 과자는?", "초코비"));
        QUIZ_LIST.add(new DailyQuizVO("애니메이션 '코코'의 배경이 된 축제는 '○들의 날'?", "죽은자"));
        QUIZ_LIST.add(new DailyQuizVO("'심슨 가족' 중 도넛을 좋아하는 아빠 이름은?", "호머"));
        QUIZ_LIST.add(new DailyQuizVO("'슈렉'과 결혼하는 공주의 이름은?", "피오나"));
        QUIZ_LIST.add(new DailyQuizVO("'인사이드 아웃'의 노란색 감정 캐릭터 이름은?", "기쁨"));
        QUIZ_LIST.add(new DailyQuizVO("'미니언즈'가 가장 좋아하는 노란색 과일은?", "바나나"));
        QUIZ_LIST.add(new DailyQuizVO("'스폰지밥'의 직장 '집게리아'에서의 보직은?", "요리사"));
        QUIZ_LIST.add(new DailyQuizVO("'하울의 움직이는 성'의 불의 악마 이름은?", "캘시퍼"));
        QUIZ_LIST.add(new DailyQuizVO("'주토피아' 주인공인 토끼 경찰의 이름은?", "주디"));

        // [영화 심화 상식]
        QUIZ_LIST.add(new DailyQuizVO("역대 전 세계 흥행 1위, 파란 피부의 외계인이 나오는 영화는?", "아바타"));
        QUIZ_LIST.add(new DailyQuizVO("스파이더맨 대사 '큰 힘에는 큰 ○○이 따른다'?", "책임"));
        QUIZ_LIST.add(new DailyQuizVO("공포 영화 '그것'의 광대 이름은?", "페니와이즈"));
        QUIZ_LIST.add(new DailyQuizVO("영화 '인셉션'에서 현실을 확인하는 도구 '토템'의 모양은?", "팽이"));
        QUIZ_LIST.add(new DailyQuizVO("'나홀로 집에' 케빈이 도둑을 잡는 날은?", "크리스마스"));
        QUIZ_LIST.add(new DailyQuizVO("매트릭스에서 진실을 보게 해주는 약의 색깔은?", "빨간색"));
        QUIZ_LIST.add(new DailyQuizVO("존 윅이 복수를 결심하게 된 죽은 반려동물은?", "강아지"));
        QUIZ_LIST.add(new DailyQuizVO("영화 '킹스맨' 대사 '○○이 사람을 만든다'?", "매너"));
        QUIZ_LIST.add(new DailyQuizVO("'주라기 공원'에서 되살아난 고대 생물은?", "공룡"));
        QUIZ_LIST.add(new DailyQuizVO("영화가 끝나고 화면에 흐르는 제작진 명단은?", "엔딩크레딧"));
    }

    private String getTodayStr() {
        return LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    // 1. 퀘스트 목록 조회 (진행도 합산)
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

    // 2. 랜덤 퀴즈 추출 (DailyQuizVO 반환)
    public DailyQuizVO getRandomQuiz() {
        return QUIZ_LIST.get(RANDOM.nextInt(QUIZ_LIST.size()));
    }

    // 3. 정답 검증 및 진행도 갱신
    @Transactional
    public boolean checkQuizAndProgress(String memberId, String userAnswer, String correctAnswer) {
        if (userAnswer == null) return false;
        
        String cleanUser = userAnswer.replace(" ", "").toLowerCase();
        String cleanCorrect = correctAnswer.replace(" ", "").toLowerCase();

        if (cleanUser.contains(cleanCorrect)) {
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

    // 5. 보상 수령
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

        if (questDao.updateRewardStatus(memberId, type, getTodayStr()) > 0) {
            memberDao.upPoint(MemberDto.builder().memberId(memberId).memberPoint(targetQuest.getReward()).build());
            return targetQuest.getReward();
        }
        return 0;
    }

    // --- [Helper 매퍼] ---
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