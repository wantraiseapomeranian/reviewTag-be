package com.kh.finalproject.dao;

import java.util.List;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.kh.finalproject.dto.InventoryDto;

@Repository
public class InventoryDao {

    @Autowired
    private SqlSession sqlSession;

    public int insert(InventoryDto inventoryDto) {
        return sqlSession.insert("inventory.insert", inventoryDto);
    }

    public boolean update(InventoryDto inventoryDto) {
        return sqlSession.update("inventory.update", inventoryDto) > 0;    
    }

    public List<InventoryDto> selectList(String memberId) { 
        InventoryDto param = new InventoryDto();
        param.setInventoryMemberId(memberId);
        return sqlSession.selectList("inventory.selectList", param);
    }


    public InventoryDto selectOne(long inventoryNo) {
        return sqlSession.selectOne("inventory.selectOne", inventoryNo);
    }
    
    public List<InventoryDto> selectListByMemberId(String memberId) {
        return sqlSession.selectList("inventory.selectListByMemberId", memberId);
    }

     public int selectCountMyItem(String memberId, long itemNo) {
        InventoryDto params = new InventoryDto();
        params.setInventoryMemberId(memberId);
        params.setInventoryItemNo(itemNo); 
        return sqlSession.selectOne("inventory.selectCountMyItem", params);
    }

  
    public boolean delete(long inventoryNo) {
        return sqlSession.delete("inventory.delete", inventoryNo) > 0;
    }

	public void unequipByType(String memberId, String type) {
		// TODO Auto-generated method stub
		
	}
}