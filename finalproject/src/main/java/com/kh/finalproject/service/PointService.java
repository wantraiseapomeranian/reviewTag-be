package com.kh.finalproject.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.finalproject.dao.InventoryDao;
import com.kh.finalproject.dao.MemberDao;
import com.kh.finalproject.dao.MemberIconDao;
import com.kh.finalproject.dao.PointHistoryDao;
import com.kh.finalproject.dao.PointItemStoreDao;
import com.kh.finalproject.dao.PointWishlistDao;
import com.kh.finalproject.dto.InventoryDto;
import com.kh.finalproject.dto.MemberDto;
import com.kh.finalproject.dto.PointHistoryDto;
import com.kh.finalproject.dto.PointItemStoreDto;
import com.kh.finalproject.dto.PointWishlistDto;
import com.kh.finalproject.vo.MemberPointVO;
import com.kh.finalproject.vo.PointHistoryPageVO;
import com.kh.finalproject.vo.PointItemWishVO;

@Service
public class PointService {

    @Autowired private PointItemStoreDao pointItemDao;
    @Autowired private MemberDao memberDao;
    @Autowired private InventoryDao inventoryDao; 
    @Autowired private PointHistoryDao pointHistoryDao;
    @Autowired private PointWishlistDao pointWishlistDao;
    @Autowired private DailyQuestService dailyQuestService;
    @Autowired private MemberIconDao memberIconDao;

    // [1] 포인트 증감 공통 메서드
    @Transactional
    public boolean addPoint(String loginId, int amount, String trxType, String reason) {
        MemberDto currentMember = memberDao.selectOne(loginId);
        if (currentMember == null) throw new RuntimeException("회원 정보가 없습니다.");

        // 차감 시 잔액 검증
        if (amount < 0 && (currentMember.getMemberPoint() + amount) < 0) {
            throw new RuntimeException("보유 포인트가 부족합니다.");
        }

        // DB에 포인트 업데이트 (증감분 전달)
        MemberDto updateDto = MemberDto.builder().memberId(loginId).memberPoint(amount).build();
        
        if (memberDao.upPoint(updateDto)) {
            pointHistoryDao.insert(PointHistoryDto.builder()
                .pointHistoryMemberId(loginId)
                .pointHistoryAmount(amount)
                .pointHistoryTrxType(trxType)
                .pointHistoryReason(reason)
                .build());
            return true;
        }
        return false;
    }

    // [2] 상점 아이템 구매
    @Transactional
    public void purchaseItem(String loginId, long itemNo) {
        PointItemStoreDto item = pointItemDao.selectOneNumber(itemNo);
        if (item == null) throw new RuntimeException("상품 정보가 없습니다.");
        if (item.getPointItemStock() <= 0) throw new RuntimeException("품절된 상품입니다.");

        // 일일 구매 제한 체크
        if (item.getPointItemDailyLimit() > 0) {
            int todayCount = pointHistoryDao.countTodayPurchase(loginId, item.getPointItemName());
            if (todayCount >= item.getPointItemDailyLimit()) throw new RuntimeException("일일 구매 제한 초과");
        }

        // 포인트 차감 및 이력 남기기
        addPoint(loginId, -(int)item.getPointItemPrice(), "USE", "아이템 구매: " + item.getPointItemName());

        // 재고 감소 및 인벤토리 지급
        item.setPointItemStock(item.getPointItemStock() - 1);
        pointItemDao.update(item);
        giveItemToInventory(loginId, itemNo);
    }

    // [3] 포인트 후원 (추가됨)
    @Transactional
    public void donatePoints(String loginId, String targetId, int amount) {
        if (amount <= 0) throw new RuntimeException("후원 금액은 0보다 커야 합니다.");
        if (loginId.equals(targetId)) throw new RuntimeException("자신에게 후원할 수 없습니다.");

        // 1. 보내는 사람 차감
        addPoint(loginId, -amount, "USE", targetId + "님에게 후원");
        // 2. 받는 사람 증가
        addPoint(targetId, amount, "GET", loginId + "님으로부터 후원");
    }

    // [4] 아이템 선물하기 (추가됨)
    @Transactional
    public void giftItem(String loginId, String targetId, long itemNo) {
        PointItemStoreDto item = pointItemDao.selectOneNumber(itemNo);
        if (item == null || item.getPointItemStock() <= 0) throw new RuntimeException("선물 가능한 상품이 없습니다.");

        // 1. 보낸 사람 포인트 차감
        addPoint(loginId, -(int)item.getPointItemPrice(), "USE", targetId + "님에게 선물: " + item.getPointItemName());
        
        // 2. 재고 감소 및 상대방 인벤토리 지급
        item.setPointItemStock(item.getPointItemStock() - 1);
        pointItemDao.update(item);
        giveItemToInventory(targetId, itemNo);
    }

    // [5] 내 포인트 요약 정보 조회 (추가됨)
    public MemberPointVO getMyPointInfo(String id) {
        MemberDto m = memberDao.selectMap(id);
        if (m == null) return null;

        // 아이콘은 이미지가 필요하므로 Src 유지, 나머지는 Style(Content) 조회
        String iconSrc = memberIconDao.selectEquippedIconSrc(id);
        String frameStyle = memberIconDao.selectEquippedFrameStyle(id); 
        String bgStyle = memberIconDao.selectEquippedBgStyle(id); 
        String nickStyle = memberIconDao.selectEquippedNickStyle(id);

        if (iconSrc == null) iconSrc = "https://i.postimg.cc/Wb3VBy9v/null.png";

        return MemberPointVO.builder()
                .nickname(m.getMemberNickname())
                .point(m.getMemberPoint())
                .iconSrc(iconSrc)
                .frameSrc(frameStyle) // VO의 필드명은 유지하되 데이터는 클래스명 전달
                .bgSrc(bgStyle)
                .nickStyle(nickStyle)
                .build();
    }
    // [6] 아이템 사용 및 장착
    @Transactional
    public void useItem(String loginId, long inventoryNo, String extraValue) {
        InventoryDto inven = inventoryDao.selectOne(inventoryNo);
        if (inven == null || !inven.getInventoryMemberId().equals(loginId)) throw new RuntimeException("아이템 권한 없음");

        PointItemStoreDto item = pointItemDao.selectOneNumber(inven.getInventoryItemNo());
        String type = item.getPointItemType();

        switch (type) {
            case "CHANGE_NICK":
                if (extraValue == null || extraValue.trim().isEmpty()) throw new RuntimeException("새 닉네임을 입력하세요.");
                memberDao.updateNickname(MemberDto.builder().memberId(loginId).memberNickname(extraValue).build());
                decreaseInventoryOrDelete(inven);
                break;    
            case "DECO_NICK": case "DECO_BG": case "DECO_ICON": case "DECO_FRAME":
                unequipByType(loginId, type); 
                inven.setInventoryEquipped("Y");
                inventoryDao.update(inven);
                break;
            case "VOUCHER":
                addPoint(loginId, (int)item.getPointItemPrice(), "GET", "상품권 사용");
                decreaseInventoryOrDelete(inven);
                break;
        }
    }

    // [7] 기타 유틸리티 및 환불/삭제 로직
    @Transactional
    public void cancelItem(String loginId, long inventoryNo) {
        InventoryDto inven = inventoryDao.selectOne(inventoryNo);
        if (inven == null || !inven.getInventoryMemberId().equals(loginId)) throw new RuntimeException("환불 권한 없음");
        
        PointItemStoreDto item = pointItemDao.selectOneNumber(inven.getInventoryItemNo());
        addPoint(loginId, (int)item.getPointItemPrice(), "GET", "환불: " + item.getPointItemName());

        item.setPointItemStock(item.getPointItemStock() + 1);
        pointItemDao.update(item);
        decreaseInventoryOrDelete(inven);
    }

    private void giveItemToInventory(String loginId, long itemNo) {
        InventoryDto existing = inventoryDao.selectOneByMemberAndItem(loginId, itemNo);
        if (existing != null) {
            existing.setInventoryQuantity(existing.getInventoryQuantity() + 1);
            inventoryDao.update(existing);
        } else {
            inventoryDao.insert(InventoryDto.builder()
                .inventoryMemberId(loginId)
                .inventoryItemNo(itemNo)
                .inventoryQuantity(1)
                .inventoryEquipped("N")
                .build());
        }
    }

    private void decreaseInventoryOrDelete(InventoryDto inven) {
        if (inven.getInventoryQuantity() > 1) {
            inven.setInventoryQuantity(inven.getInventoryQuantity() - 1);
            inventoryDao.update(inven);
        } else {
            inventoryDao.delete(inven.getInventoryNo());
        }
    }

    @Transactional
    public void unequipItem(String loginId, long inventoryNo) { 
        InventoryDto inv = inventoryDao.selectOne(inventoryNo);
        if(inv != null && loginId.equals(inv.getInventoryMemberId())) {
            inv.setInventoryEquipped("N"); 
            inventoryDao.update(inv);
        }
    }

    private void unequipByType(String loginId, String type) {
        List<InventoryDto> list = inventoryDao.selectListByMemberId(loginId);
        for (InventoryDto dto : list) {
            if ("Y".equals(dto.getInventoryEquipped())) {
                PointItemStoreDto info = pointItemDao.selectOneNumber(dto.getInventoryItemNo());
                if (info != null && type.equals(info.getPointItemType())) {
                    dto.setInventoryEquipped("N");
                    inventoryDao.update(dto);
                }
            }
        }
    }

    public PointHistoryPageVO getHistoryList(String loginId, int page, String type) {
        int size = 10;
        int startRow = (page - 1) * size + 1;
        int endRow = page * size;
        List<PointHistoryDto> list = pointHistoryDao.selectListByMemberIdPaging(loginId, startRow, endRow, type);
        int totalCount = pointHistoryDao.countHistory(loginId, type);
        return PointHistoryPageVO.builder().list(list).totalCount(totalCount).totalPage((totalCount + size - 1) / size).currentPage(page).build();
    }

    @Transactional public int playRoulette(String loginId) {
        InventoryDto ticket = inventoryDao.selectListByMemberId(loginId).stream()
                .filter(i -> "RANDOM_ROULETTE".equals(pointItemDao.selectOneNumber(i.getInventoryItemNo()).getPointItemType()))
                .findFirst().orElseThrow(() -> new RuntimeException("룰렛 티켓이 없습니다."));
        int idx = (int)(Math.random() * 6);
        int reward = (idx == 4) ? 2000 : (idx == 0) ? 1000 : 0;
        decreaseInventoryOrDelete(ticket);
        if (reward > 0) addPoint(loginId, reward, "GET", "룰렛 당첨");
        dailyQuestService.questProgress(loginId, "ROULETTE");
        return idx;
    }

    // 관리자 기능 및 위시리스트
    @Transactional public void addItem(String loginId, PointItemStoreDto d) { pointItemDao.insert(d); }
    @Transactional public void editItem(String loginId, PointItemStoreDto d) { pointItemDao.update(d); }
    @Transactional public void deleteItem(String loginId, long itemNo) { pointItemDao.delete(itemNo); }
    @Transactional public void discardItem(String loginId, long inventoryNo) { inventoryDao.delete(inventoryNo); }
    @Transactional public boolean toggleWish(String loginId, long itemNo) {
        PointItemWishVO vo = PointItemWishVO.builder().memberId(loginId).itemNo(itemNo).build();
        if (pointWishlistDao.checkWish(vo) > 0) { pointWishlistDao.delete(vo); return false; }
        else { pointWishlistDao.insert(vo); return true; }
    }
    public List<Long> getMyWishItemNos(String loginId) { return pointWishlistDao.selectMyWishItemNos(loginId); }
    public List<PointWishlistDto> getMyWishlist(String loginId) { return pointWishlistDao.selectMyWishlist(loginId); }
}