package com.kh.finalproject.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.finalproject.dto.AttendanceStatusDto;
import com.kh.finalproject.service.AttendanceService;

@RestController
@RequestMapping("/point/main/attendance")
@CrossOrigin // CORS 허용
public class AttendanceRestController {

	@Autowired
	private AttendanceService attendanceService;
	
	// [수정 1] memberId -> loginId
	@GetMapping("/status")
	public ResponseEntity<AttendanceStatusDto> getStatus(
			@RequestAttribute String loginId) { 
		
		// 서비스로 넘길 땐 변수명 달라도 값만 맞으면 됨	
		AttendanceStatusDto status = attendanceService.getMyStatus(loginId);
		return ResponseEntity.ok(status);
	}
	
	// [정상] 여기는 loginId로 잘 하셨습니다.
	@PostMapping("/check")
	public ResponseEntity<String> doCheck(
			@RequestAttribute String loginId) {
		
		try {
			attendanceService.checkAttendance(loginId);
			// 프론트에서 success 문자열로 성공 여부 판단하므로 형식 맞춤
			return ResponseEntity.ok("success:10"); 
		} catch (IllegalStateException e) {
			return ResponseEntity.ok("fail:" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.ok("fail:에러 발생");
		}
	}

	// [수정 2] memberId -> loginId
	@GetMapping("/calendar")
	public ResponseEntity<List<String>> getCalendar(
			@RequestAttribute String loginId) { 
		
		List<String> dates = attendanceService.getMyAttendanceDates(loginId);
		return ResponseEntity.ok(dates);
	}
}