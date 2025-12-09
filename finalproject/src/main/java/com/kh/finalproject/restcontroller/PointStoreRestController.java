package com.kh.finalproject.restcontroller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.kh.finalproject.dao.PointInventoryDao;
import com.kh.finalproject.dao.PointItemDao;
import com.kh.finalproject.dto.PointInventoryDto;
import com.kh.finalproject.dto.PointItemDto;
import com.kh.finalproject.service.PointService;
import com.kh.finalproject.vo.*;

@RestController
@RequestMapping("/point/store") // ★ 상점 관련 주소
@CrossOrigin
public class PointStoreRestController {

    @Autowired private PointService pointService;
    @Autowired private PointItemDao pointItemDao;
    @Autowired private PointInventoryDao pointInventoryDao;

    // 1. 상품 목록
    @GetMapping("/")
    public List<PointItemDto> list() { return pointItemDao.selectList(); }

    // 2. 구매
    @PostMapping("/buy")
    public String buy(@RequestAttribute String loginId, @RequestBody PointBuyVO vo) {
        try {
            
            pointService.purchaseItem(loginId, vo.getBuyItemNo());
            return "success";
        } catch (Exception e) { e.printStackTrace(); return "fail"; }
    }

    // 3. 선물
    @PostMapping("/gift")
    public String gift(@RequestAttribute String loginId, @RequestBody PointGiftVO vo) {
        try {
            pointService.giftItem(loginId, vo.getTargetId(), vo.getItemNo());
            return "success";
        } catch (Exception e) { e.printStackTrace(); return "fail"; }
    }

    // 4. 취소 (환불)
    @PostMapping("/cancel")
    public String cancel(@RequestAttribute String loginId, @RequestBody PointCancelVO vo) {
        try {
            pointService.cancelItem(loginId, vo.getInventoryNo());
            return "success";
        } catch (Exception e) { e.printStackTrace(); return "fail"; }
    }

    // 5. 내 보관함 조회 
    @GetMapping("/inventory/my")
    public List<PointInventoryDto> myInventory(@RequestAttribute String loginId) {
        return pointInventoryDao.selectListByMemberId(loginId);
    }

    // 6. [관리자] 상품 등록
    @PostMapping("/item/add")
    public String addItem(@RequestAttribute String loginId, @RequestBody PointItemDto itemDto) {
        try {
            pointService.addItem(loginId, itemDto);
            return "success";
        } catch (Exception e) { e.printStackTrace(); return "fail"; }
    }
    
 // 7. [관리자] 상품 수정
    @PostMapping("/item/edit")
    public String editItem(@RequestAttribute String loginId, @RequestBody PointItemDto itemDto) {
        try {
            pointService.editItem(loginId, itemDto);
            return "success";
        } catch (Exception e) { e.printStackTrace(); return "fail"; }
    }
    //8.[관리자]상품삭제
    @PostMapping("/item/delete")
    public String deleteItem(
            @RequestAttribute String loginId, 
            @RequestBody PointItemDto itemDto // ★ 수정됨: Map -> DTO
    ) {
        try {
            // DTO에서 번호(PK)를 꺼내서 서비스로 전달
            pointService.deleteItem(loginId, itemDto.getPointItemNo());
            return "success";
        } catch (Exception e) { 
            e.printStackTrace(); 
            return "fail"; 
        }
    }
    @PostMapping("/inventory/delete")
    public String discardItem(
            @RequestAttribute String loginId, 
            @RequestBody PointCancelVO vo // 기존 VO 재활용 (inventoryNo만 있으면 됨)
    ) {
        try {
            // int 캐스팅 주의 (VO가 long이면 int로 변환)
            pointService.discardItem(loginId, (int)vo.getInventoryNo());
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
 // 10. [인벤토리] 아이템 사용
    // 요청 JSON: { "inventoryNo": 123, "extraValue": "새닉네임" }
    @PostMapping("/inventory/use")
    public String useItem(
            @RequestAttribute String loginId, 
            @RequestBody PointUseVO vo // ★ 수정됨: Map -> VO
    ) {
        try {
            // VO에서 깔끔하게 꺼내서 서비스로 전달
            pointService.useItem(loginId, vo.getInventoryNo(), vo.getExtraValue());
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail:" + e.getMessage();
        }
    }
    
}