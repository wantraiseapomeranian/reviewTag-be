package com.kh.finalproject.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.finalproject.dao.AttendanceHistoryDao;
import com.kh.finalproject.dao.AttendanceStatusDao;
import com.kh.finalproject.dto.AttendanceHistoryDto;
import com.kh.finalproject.dto.AttendanceStatusDto;
import com.kh.finalproject.vo.TokenVO;

@Service
public class AttendanceService {

	@Autowired
	private AttendanceStatusDao statusDao; // 현황판 DAO
	
	@Autowired
	private AttendanceHistoryDao historyDao; // 기록용 DAO
	
	@Autowired
	private PointService pointService; // 포인트 서비스 주입
	
	
	// 출석체크 수행 (트랜잭션: 도중 에러나면 전체 롤백)
	@Transactional
	public void checkAttendance(String loginId) {
		
		
		// 1. 내 현황 가져오기
		AttendanceStatusDto status = statusDao.selectOne(loginId);
		
		// === 신규 유저 (첫 출석) ===
		if(status == null) {
			// Status 생성
			AttendanceStatusDto newStatus = AttendanceStatusDto.builder()
					.attendanceStatusMemberId(loginId)
					.build();
			statusDao.insert(newStatus);
			
			// History 기록
			recordHistory(loginId);
			
			// 포인트 지급 (첫 출석 500점)
			pointService.addAttendancePoint(loginId, 100, "첫 출석 환영 보너스");
			return;
		}
		
		// === 기존 유저 (날짜 비교) ===
		LocalDate today = LocalDate.now();
		// DB Timestamp -> LocalDate 변환
		LocalDate lastDate = status.getAttendanceStatusLastdate().toLocalDateTime().toLocalDate();
		
		// 1) 오늘 이미 했는지?
		if(today.equals(lastDate)) {
			throw new IllegalStateException("이미 오늘 출석체크를 완료했습니다.");
		}
		
		// 2) 어제 했는지? (연속 여부)
		int currentStreak = 1; // 끊겼으면 1부터 다시 시작
		if(today.minusDays(1).equals(lastDate)) {
			currentStreak = status.getAttendanceStatusCurrent() + 1;
		}
		
		// 3) 최고 기록 갱신 여부
		int maxStreak = status.getAttendanceStatusMax();
		if(currentStreak > maxStreak) {
			maxStreak = currentStreak;
		}
		
		// 4) Status 업데이트
		status.setAttendanceStatusCurrent(currentStreak);
		status.setAttendanceStatusMax(maxStreak);
		statusDao.update(status); // Total은 쿼리에서 +1 됨
		
		// 5) History 기록
		recordHistory(loginId);
		
		// 6) 포인트 계산 및 지급
		int point = 100; // 기본
		if(currentStreak % 7 == 0) {
			point += 50; // 7일마다 보너스
		}
		
		pointService.addAttendancePoint(loginId, point, "일일 출석 ("+currentStreak+"일 연속)");
	}
	
	// 내 정보 조회
	public AttendanceStatusDto getMyStatus(String loginId) {
		return statusDao.selectOne(loginId);
	}
	
	// (내부용) 히스토리 저장 헬퍼 메소드
	private void recordHistory(String loginId) {
		AttendanceHistoryDto history = AttendanceHistoryDto.builder()
				.attendanceHistoryMemberId(loginId)
				.build();
		historyDao.insert(history);
	}
	
	//달력출력용
	
	public List<String> getMyAttendanceDates(String loginId) {
        return historyDao.selectCalendarDates(loginId);
    }
}