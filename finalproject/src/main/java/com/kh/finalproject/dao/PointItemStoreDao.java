package com.kh.finalproject.dao;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.kh.finalproject.dto.PointItemStoreDto;

@Repository
public class PointItemStoreDao {
    
    @Autowired
    private SqlSession sqlSession;


    public int sequence() {
        return sqlSession.selectOne("pointitemstore.sequence");
    }

    public int insert(PointItemStoreDto pointItemDto) {
        return sqlSession.insert("pointitemstore.insert", pointItemDto);
    }

    public boolean update(PointItemStoreDto pointItemDto) {
        return sqlSession.update("pointitemstore.update", pointItemDto) > 0;
    }

    public List<PointItemStoreDto> selectList() {
        return sqlSession.selectList("pointitemstore.selectList");
    }
  
    public PointItemStoreDto selectOneNumber(long pointItemNo) {
        return sqlSession.selectOne("pointitemstore.selectOneNumber", pointItemNo);
    }

    public boolean delete(long pointItemNo) { 
        return sqlSession.delete("pointitemstore.delete", pointItemNo) > 0;
    }
    }