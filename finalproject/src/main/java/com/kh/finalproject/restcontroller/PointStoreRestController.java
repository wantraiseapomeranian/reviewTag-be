package com.kh.finalproject.restcontroller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kh.finalproject.dao.InventoryDao;
import com.kh.finalproject.dao.MemberDao;
import com.kh.finalproject.dao.PointHistoryDao;
import com.kh.finalproject.dao.PointItemStoreDao;
import com.kh.finalproject.dto.InventoryDto;
import com.kh.finalproject.dto.PointItemStoreDto;
import com.kh.finalproject.dto.PointWishlistDto;
import com.kh.finalproject.service.PointService;
import com.kh.finalproject.vo.*;

@RestController
@RequestMapping("/point/main/store")
@CrossOrigin
public class PointStoreRestController {

    @Autowired private PointService pointService;
    @Autowired private PointItemStoreDao pointItemDao;
    @Autowired private InventoryDao inventoryDao;
    @Autowired private MemberDao memberDao;
    @Autowired private PointHistoryDao pointHistoryDao;

    // =========================================================
    // [1] 상점 (상품 조회, 구매, 선물)
    // =========================================================

    // 1. 상품 목록 조회
    @GetMapping("")
    public List<PointItemStoreDto> list() { 
        return pointItemDao.selectList(); 
    }

    // 2. 구매 (POST /point/main/store/buy)
    @PostMapping("/buy")
    public String buy(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointBuyVO vo) {
        
        if(loginId == null) return "fail:로그인 정보 없음";
        try {
            pointService.purchaseItem(loginId, vo.getBuyItemNo());
            return "success";
        } catch (Exception e) { 
            e.printStackTrace(); 
            return "fail:" + e.getMessage(); // 에러 메시지 전달
        }
    }

    // 3. 선물 (POST /point/main/store/gift)
    @PostMapping("/gift")
    public String gift(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointGiftVO vo) {
        
        if(loginId == null) return "fail:로그인 정보 없음";
        try {
            pointService.giftItem(loginId, vo.getTargetId(), vo.getItemNo());
            return "success";
        } catch (Exception e) { 
            e.printStackTrace(); 
            return "fail:" + e.getMessage();
        }
    }

    // =========================================================
    // [2] 인벤토리 (보관함 조회, 사용, 환불, 버리기)
    // =========================================================

    // 4. 내 보관함 조회 (GET /point/main/store/inventory/my)
    @GetMapping("/inventory/my")
    public List<InventoryDto> myInventory(
            @RequestAttribute(value="loginId", required=false) String loginId) {
        if(loginId == null) return List.of();
        return inventoryDao.selectListByMemberId(loginId);
    }

    // 5. 아이템 사용 (POST /point/main/store/inventory/use)
    @PostMapping("/inventory/use")
    public String useItem(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointUseVO vo) {
        if(loginId == null) return "fail:로그인 필요";
        try {
            pointService.useItem(loginId, vo.getInventoryNo(), vo.getExtraValue());
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail:" + e.getMessage();
        }
    }
    
    // 6. 구매 취소/환불 (POST /point/main/store/cancel)
    @PostMapping("/cancel")
    public String cancel(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointCancelVO vo) {
        if(loginId == null) return "fail:로그인 정보 없음";
        try {
            pointService.cancelItem(loginId, vo.getInventoryNo());
            return "success";
        } catch (Exception e) { 
            e.printStackTrace(); 
            return "fail:" + e.getMessage(); 
        }
    }
    
    // 7. 아이템 삭제/버리기 (POST /point/main/store/inventory/delete)
    @PostMapping("/inventory/delete")
    public String discardItem(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointCancelVO vo) {
        if(loginId == null) return "fail";
        try {
            pointService.discardItem(loginId, vo.getInventoryNo());
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // =========================================================
    // [3] 위시리스트 (찜)
    // =========================================================

    // 8. 찜 토글 (추가/삭제 자동) (POST /point/main/store/wish/toggle)
    @PostMapping("/wish/toggle")
    public boolean toggleWish(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointItemWishVO vo) {
        if(loginId == null) return false;
        return pointService.toggleWish(loginId, vo.getItemNo());
    }

    // 9. 내가 찜한 아이템 번호만 조회 (GET /point/main/store/wish/check)
    @GetMapping("/wish/check")
    public List<Long> myWishItemNos(
            @RequestAttribute(value="loginId", required=false) String loginId) {
        if(loginId == null) return List.of();
        return pointService.getMyWishItemNos(loginId);
    }

    // 10. 내 찜 목록 상세 조회 (GET /point/main/store/wish/my)
    @GetMapping("/wish/my")
    public List<PointWishlistDto> myWishlist(
            @RequestAttribute(value="loginId", required=false) String loginId) {
        if(loginId == null) return List.of();
        return pointService.getMyWishlist(loginId);
    }
    
    // 11. 찜 삭제 (POST /point/main/store/wish/delete)
    @PostMapping("/wish/delete")
    public String deleteWish(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointItemWishVO vo) {
        if(loginId == null) return "fail:로그인 정보 없음";
        try {
            pointService.deleteWish(loginId, vo.getItemNo());
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // =========================================================
    // [4] 기타 기능 (룰렛, 내정보, 관리자)
    // =========================================================

    // 12. 룰렛 돌리기 (POST /point/main/store/roulette)
    @PostMapping("/roulette")
    public ResponseEntity<Integer> startRoulette(
            @RequestAttribute(value="loginId", required=false) String loginId) {
        
        if(loginId == null) throw new RuntimeException("로그인이 필요합니다.");

        // 서비스가 0~5 사이 인덱스를 리턴
        int resultIndex = pointService.playRoulette(loginId);
        
        return ResponseEntity.ok(resultIndex); 
    }
    // 13. 내 포인트 및 장착 정보 조회 (GET /point/main/store/my-info)
    @GetMapping("/my-info")
    public MemberPointVO getMyInfo(
            @RequestAttribute(value="loginId", required=false) String loginId) {
        if (loginId == null) return null; 
        return pointService.getMyPointInfo(loginId);
    }
    
    // 14. [관리자] 상품 등록
    @PostMapping("/item/add")
    public String addItem(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointItemStoreDto itemDto) {
        if(loginId == null) return "fail";
        try {
            pointService.addItem(loginId, itemDto);
            return "success";
        } catch (Exception e) { 
            e.printStackTrace(); 
            return "fail"; 
        }
    }
    
    // 15. [관리자] 상품 수정
    @PostMapping("/item/edit")
    public String editItem(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointItemStoreDto itemDto) {
        if(loginId == null) return "fail";
        try {
            pointService.editItem(loginId, itemDto);
            return "success";
        } catch (Exception e) { 
            e.printStackTrace(); 
            return "fail"; 
        }
    }
    
    // 16. [관리자] 상품 삭제
    @PostMapping("/item/delete")
    public String deleteItem(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointItemStoreDto itemDto) {
        if(loginId == null) return "fail";
        try {
            pointService.deleteItem(loginId, itemDto.getPointItemNo());
            return "success";
        } catch (Exception e) { 
            e.printStackTrace(); 
            return "fail"; 
        }
    }
    //장착해제
    @PostMapping("/inventory/unequip")
    public String unequipItem(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointUseVO vo) { // inventoryNo를 받기 위해 PointUseVO 사용
        if(loginId == null) return "fail:로그인 필요";
        try {
            // 서비스에서 장착 해제 로직 실행
            pointService.unequipItem(loginId, vo.getInventoryNo());
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail:" + e.getMessage();
        }
    }
}