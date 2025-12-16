package com.kh.finalproject.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.finalproject.dao.HeartDao;
import com.kh.finalproject.dto.HeartDto;
import com.kh.finalproject.error.NeedPermissionException;

@Service
public class HeartService {

	@Autowired
	private HeartDao heartDao;
	
	//하트 충전
	@Transactional
	public void checkAndRefill(String memberId) {
		
		//회원인지 조회
		if(memberId == null) throw new NeedPermissionException();
		
		//내 하트 정보 조회
		HeartDto heartDto = heartDao.selectHeart(memberId);
		
		//지갑이 없으면 생성
		if(heartDto == null) {
			heartDao.createHeartWallet(memberId);
			return;
		}
		
		//날짜 비교
		String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
		
		//하루가 지났으면
		if(!today.equals(heartDto.getHeartLastRefillDate())) {
			
			//리필 처리
			if(heartDto.getHeartCount() < heartDto.getHeartMax()) {
				//5개 미만 보유자: 5개로 충전 + 날짜 갱신
				heartDao.dailyRefill(memberId);
			} else {
				//5개 이상 보유자: 개수는 유지 + 날짜만 갱신
				heartDao.updateRefillDateOnly(memberId);
			}
		}
	}
	
	//퀴즈 시작 시 하트 차감
	@Transactional
	public void useHeartForQuiz(String memberId) {
		//회원인지 조회
		if(memberId == null) throw new NeedPermissionException();
		
		//상태 최신화
		checkAndRefill(memberId);
		
		//하트 1개 차감 시도
		int result = heartDao.decreaseHeart(memberId);
		
		//실패 시 예외 발생
		if(result == 0) {
			throw new IllegalStateException("하트가 부족하여 퀴즈를 시작할 수 없습니다.");
		}
	}
	
	//하트 충전
	@Transactional
	public void chargeHeart(String memberId, int amount) {
		
		//회원인지 조회
		if(memberId == null) throw new NeedPermissionException();
		
		// 지갑 없으면 생성
		if(heartDao.selectHeart(memberId) == null) {
			heartDao.createHeartWallet(memberId);
		}
		
		// 무조건 증가
		heartDao.increaseHeart(memberId, amount);
	}
	
	//화면 표시용 하트 정보 조회
	@Transactional
	public HeartDto getHeartInfo(String memberId) {
		
		//회원인지 조회
		if(memberId == null) throw new NeedPermissionException();
		
		//리필 조회
		checkAndRefill(memberId);
		
		//하트 표시
		return heartDao.selectHeart(memberId);
	}
	
}
