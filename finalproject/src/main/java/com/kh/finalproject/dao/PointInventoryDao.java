package com.kh.finalproject.dao;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.kh.finalproject.dto.PointInventoryDto;

@Repository
public class PointInventoryDao {

    @Autowired
    private SqlSession sqlSession;

    /* 1. 등록 C */

    public int insert(PointInventoryDto pointInventoryDto) {
        return sqlSession.insert("pointinventory.insert", pointInventoryDto);
    }

    /* 2. 업데이트 U */
    public boolean update(PointInventoryDto pointInventoryDto) {
        return sqlSession.update("pointinventory.update", pointInventoryDto) > 0;	
    }

    /* 3. 조회 R */
  
    public List<PointInventoryDto> selectList(String memberId) { 
         return sqlSession.selectList("pointinventory.selectList", memberId);
    }

    // 상세 조회
    public PointInventoryDto selectOneNumber(long pointInventoryNo) {
        return sqlSession.selectOne("pointinventory.selectOneNumber", pointInventoryNo);
    }

    /* 4. 삭제 D */
   
    public boolean delete(long pointInventoryNo) {
        return sqlSession.delete("pointinventory.delete", pointInventoryNo) > 0;
    }

    public List<PointInventoryDto> selectListByMemberId(String loginId) {
        return sqlSession.selectList("pointinventory.selectListByMemberId", loginId);
    }

public int selectCountMyItem(String receiverId, long itemNo) {
        
        // 1. 검색 조건을 담을 빈 DTO 객체 생성
        PointInventoryDto params = new PointInventoryDto();
        
   
        params.setPointInventoryMemberId(receiverId);
        params.setPointInventoryItemNo((int)itemNo); 

        return sqlSession.selectOne("pointinventory.selectCountMyItem", params);
    }
}
