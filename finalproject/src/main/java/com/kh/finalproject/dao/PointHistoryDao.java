package com.kh.finalproject.dao;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.kh.finalproject.dto.PointHistoryDto;

@Repository
public class PointHistoryDao {

    @Autowired
    private SqlSession sqlSession;

    /* 1. 등록 C */
    public int insert(PointHistoryDto pointHistoryDto) { 
        return sqlSession.insert("pointhistory.insert", pointHistoryDto);
    }
    
    public int insertHistory(PointHistoryDto pointHistoryDto) { 
        return sqlSession.insert("pointhistory.inserthistory", pointHistoryDto);
    }
    /* 2. 수정 U */
    public boolean update(PointHistoryDto pointHistoryDto) {
        return sqlSession.update("pointhistory.update", pointHistoryDto) > 0;
    }

    /* 3.(1) 목록 출력 R */
    public List<PointHistoryDto> selectListByMemberId(String memberId) {
        return sqlSession.selectList("pointhistory.selectListByMemberId", memberId);
    }

    /* 3.(2) 번호 기준 조회 R */
    public PointHistoryDto selectOneNumber(long pointHistoryNo) {
        return sqlSession.selectOne("pointhistory.selectOneNumber", pointHistoryNo);
    }

    /* 4. 삭제 D */
    public boolean delete(long pointHistoryNo) {
        return sqlSession.delete("pointhistory.delete", pointHistoryNo) > 0;
    }
}