package com.kh.finalproject.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.finalproject.dto.BoardDto;

@Repository
public class BoardDao {

	@Autowired
	private SqlSession sqlSession;
	
	// 등록
	public void insert(BoardDto boardDto) {
		int boardNo = sqlSession.selectOne("board.sequence");
		boardDto.setBoardNo(boardNo);
		sqlSession.insert("board.insert",boardDto);
	}
	
	// 조회 : 전체 조회
	public List<BoardDto> selectList(){
		return sqlSession.selectList("board.selectList");
	}
	
	// 조회 : 컨텐츠별 조회
	public List<BoardDto> selesctListByContents(Long boardContentsId){
		return sqlSession.selectList("board.selectListByContents", boardContentsId);
	}
	
	// 상세조회
	public BoardDto selectOne(int boardNo) {
		return sqlSession.selectOne("board.detail", boardNo);
	}
	
	// 수정
	public boolean update(BoardDto boardDto) {
		return sqlSession.update("board.update",boardDto)>0;
	}
	
	// 삭제
	public void delete(int boardNo) {
		sqlSession.delete("board.detail", boardNo);
	}
	
}
