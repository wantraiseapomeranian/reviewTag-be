package com.kh.finalproject.dao;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.kh.finalproject.dto.PointItemDto;

@Repository
public class PointItemDao {
    
    @Autowired
    private SqlSession sqlSession;

    public int sequence() {
        return sqlSession.selectOne("pointitem.sequence");
    }

    /* 1. 등록 */

    public int insert(PointItemDto pointItemDto) {
        return sqlSession.insert("pointitem.insert", pointItemDto);
    }

    /* 2. 수정 */

    public boolean update(PointItemDto pointItemDto) {
        return sqlSession.update("pointitem.update", pointItemDto) > 0;
    }

    /* 3. 목록 조회 */

    public List<PointItemDto> selectList() {
        return sqlSession.selectList("pointitem.selectList");
    }

    /* 4. 상세 조회 */
 
    public PointItemDto selectOneNumber(long pointItemNo) {
        return sqlSession.selectOne("pointitem.selectOneNumber", pointItemNo);
    }

    /* 5. 삭제 */
    public boolean delete(long pointItemNo) { // DTO 대신 번호만 받아도 됩니다.
        return sqlSession.delete("pointitem.delete", pointItemNo) > 0;
    }
}