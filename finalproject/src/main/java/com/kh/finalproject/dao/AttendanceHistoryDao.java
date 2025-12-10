package com.kh.finalproject.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.finalproject.dto.AttendanceHistoryDto;

@Repository
public class AttendanceHistoryDao {

	@Autowired
	private SqlSession sqlSession;

	// 기록 1줄 추가 (출석 도장)
	public int insert(AttendanceHistoryDto attendanceHistoryDto) {
		return sqlSession.insert("attendanceHistory.insert", attendanceHistoryDto);
	}

	// 기록 1줄 삭제 (관리자용, 번호 기준)
	public boolean delete(int historyNo) {
		return sqlSession.delete("attendanceHistory.delete", historyNo) > 0;
	}

	// 내 기록 전체 삭제 (회원 탈퇴용, 아이디 기준)
	public boolean deleteAll(String memberId) {
		return sqlSession.delete("attendanceHistory.deleteAll", memberId) > 0;
	}

	// 내 기록 전체 조회 (리스트)
	public List<AttendanceHistoryDto> selectList(String memberId) {
		return sqlSession.selectList("attendanceHistory.selectList", memberId);
	}
	public List<String> selectCalendarDates(String memberId) {
        return sqlSession.selectList("attendanceHistory.selectCalendarDates", memberId);
    }
}