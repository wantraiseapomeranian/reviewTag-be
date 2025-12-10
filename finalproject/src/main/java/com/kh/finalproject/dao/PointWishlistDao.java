package com.kh.finalproject.dao;

import com.kh.finalproject.dto.PointWishlistDto;
import com.kh.finalproject.vo.PointItemWishVO; // ★ 새로운 VO 임포트
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PointWishlistDao {

    @Autowired
    private SqlSession sqlSession;

    // 찜 추가 (VO 사용)
    public void insert(PointItemWishVO vo) { // ★ 시그니처 변경
        sqlSession.insert("pointWishlist.insert", vo);
    }

    // 찜 삭제 (VO 사용)
    public void delete(PointItemWishVO vo) { // ★ 시그니처 변경
        sqlSession.delete("pointWishlist.delete", vo);
    }

    // 찜 여부 확인 (VO 사용)
    public int checkWish(PointItemWishVO vo) { // ★ 시그니처 변경
        return sqlSession.selectOne("pointWishlist.checkWish", vo);
    }

    // [WishlistView용] 내가 찜한 목록 전체 조회
    public List<PointWishlistDto> selectMyWishlist(String memberId) {
        return sqlSession.selectList("pointWishlist.selectMyWishlist", memberId);
    }

    // [StoreView용] 내가 찜한 아이템 번호 리스트 조회
    public List<Integer> selectMyWishItemNos(String memberId) {
        return sqlSession.selectList("pointWishlist.selectMyWishItemNos", memberId);
    }
}