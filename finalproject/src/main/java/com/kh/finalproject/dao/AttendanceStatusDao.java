package com.kh.finalproject.dao;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.finalproject.dto.AttendanceStatusDto;

@Repository
public class AttendanceStatusDao {

	@Autowired
	private SqlSession sqlSession;

	// 현황판 생성 (신규 회원)
	public int insert(AttendanceStatusDto attendanceStatusDto) {
		return sqlSession.insert("attendanceStatus.insert", attendanceStatusDto);
	}

	// 현황판 수정 (연속 출석 갱신)
	public boolean update(AttendanceStatusDto attendanceStatusDto) {
		return sqlSession.update("attendanceStatus.update", attendanceStatusDto) > 0;
	}

	// 현황판 삭제 (회원 탈퇴 등) - MemberId 기준
	public boolean delete(String memberId) {
		return sqlSession.delete("attendanceStatus.delete", memberId) > 0;
	}

	// 내 현황 조회
	public AttendanceStatusDto selectOne(String memberId) {
		return sqlSession.selectOne("attendanceStatus.selectOne", memberId);
	}
}