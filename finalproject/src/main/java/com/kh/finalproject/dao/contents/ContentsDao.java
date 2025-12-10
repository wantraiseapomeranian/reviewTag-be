package com.kh.finalproject.dao.contents;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.finalproject.dto.contents.ContentsDetailDto;
import com.kh.finalproject.vo.contents.ContentsVO;

@Repository
public class ContentsDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	//컨텐츠 저장/갱신 (contents 테이블)
    public void upsertContent(ContentsVO vo) {
    	sqlSession.insert("contents.upsertContent", vo);
    } 
    //컨텐츠 상세 조회 및 장르 이름 리스트 조회
    public ContentsDetailDto selectContentDetailWithGenres(Long contentsId) {
    	return sqlSession.selectOne("contents.selectContentDetailWithGenres", contentsId);
    }
    //장르별 컨텐츠 목록 조회(페이징)
    public List<ContentsDetailDto> selectListByGenre(Map<String, Object> params) {
    	return sqlSession.selectList("contents.selectContentsByGenre", params);
    }
    //전체 컨텐츠 목록 조회 (페이징)
    public List<ContentsDetailDto> selectContentsList(Map<String, Object> params) {
        return sqlSession.selectList("contents.selectContentsList", params);
    }
    //타입별 컨텐츠 목록 조회(페이징)
    public List<ContentsDetailDto> selectContentsListByType(Map<String, Object> params) {
    	return sqlSession.selectList("contents.selectContentsListByType", params);
    }
 }
