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
    @Autowired private MemberIconDao memberIconDao;
    @Autowired private DailyQuestService dailyQuestService;

    // [1] 유틸리티: 등급 가중치
    private int getLevelWeight(String level) {
        if (level == null) return 0;
        switch (level) {
            case "관리자": return 99;
            case "우수회원": return 2;
            case "일반회원": return 1;
            default: return 0;
        }
    }

    // [2] 포인트 증감 공통 메서드 (핵심)
    @Transactional
    public boolean addPoint(String memberId, int amount, String trxType) {
        MemberDto dto = new MemberDto();
        dto.setMemberId(memberId);
        dto.setMemberPoint(amount); 
        if (memberDao.upPoint(dto)) {
            pointHistoryDao.insert(PointHistoryDto.builder()
                .pointHistoryMemberId(memberId)
                .pointHistoryAmount(amount)
                .pointHistoryTrxType(trxType)
                .build());
            return true;
        }
        return false;
    }

    // ★ 복구: 출석 체크 및 외부 포인트 지급 로직
    @Transactional
    public void addAttendancePoint(String loginId, int amount, String memo) {
        // memo는 히스토리 trxType으로 활용하거나 로그용으로 사용
        addPoint(loginId, amount, "GET");
    }

    // [3] 상점 트랜잭션 (구매/선물)
    private void processTransaction(String senderId, String receiverId, long itemNo, String trxType) {
        PointItemStoreDto item = pointItemDao.selectOneNumber(itemNo);
        if (item == null || item.getPointItemStock() <= 0) throw new RuntimeException("상품 오류/품절");

        if (item.getPointItemIsLimitedPurchase() == 1) {
            boolean alreadyHas = inventoryDao.selectListByMemberId(receiverId).stream()
                .anyMatch(i -> i.getInventoryItemNo() == itemNo);
            if (alreadyHas) throw new RuntimeException("이미 보유 중");
        }

        addPoint(senderId, -(int)item.getPointItemPrice(), trxType);
        item.setPointItemStock(item.getPointItemStock() - 1);
        pointItemDao.update(item);

        InventoryDto inven = inventoryDao.selectListByMemberId(receiverId).stream()
            .filter(i -> i.getInventoryItemNo() == itemNo).findFirst().orElse(null);

        if (inven != null) {
            inven.setInventoryQuantity(inven.getInventoryQuantity() + 1);
            inventoryDao.update(inven);
        } else {
            InventoryDto newInven = new InventoryDto();
            newInven.setInventoryMemberId(receiverId);
            newInven.setInventoryItemNo(itemNo); 
            newInven.setInventoryQuantity(1);
            newInven.setInventoryEquipped("N");
            inventoryDao.insert(newInven);
        }
    }

    @Transactional public void purchaseItem(String id, long no) { processTransaction(id, id, no, "USE"); }
    @Transactional public void giftItem(String sid, String tid, long no) { processTransaction(sid, tid, no, "SEND"); }

    // [4] 인벤토리 관리 (사용/해제/환불)
    @Transactional
    public void useItem(String loginId, long inventoryNo, String extraValue) {
        InventoryDto inven = inventoryDao.selectOne(inventoryNo);
        PointItemStoreDto item = pointItemDao.selectOneNumber(inven.getInventoryItemNo());
        String type = item.getPointItemType();

        switch (type) {
            case "CHANGE_NICK":
                MemberDto dto = new MemberDto();
                dto.setMemberId(loginId);
                dto.setMemberNickname(extraValue);
                memberDao.updateNickname(dto);
                decreaseInventoryOrDelete(inven);
                break;
            case "VOUCHER":
            case "RANDOM_POINT":
                int amount = type.equals("VOUCHER") ? (int)item.getPointItemPrice() : (int)(Math.random()*1901)+100;
                addPoint(loginId, amount, "GET");
                decreaseInventoryOrDelete(inven);
                break;
            case "DECO_NICK":
            case "DECO_BG":
            case "DECO_ICON":
            case "DECO_FRAME":
                this.unequipByType(loginId, type);
                inven.setInventoryEquipped("Y");
                inventoryDao.update(inven);
                break;
        }
    }

    @Transactional
    public void unequipItem(String loginId, long inventoryNo) {
        InventoryDto inven = inventoryDao.selectOne(inventoryNo);
        if (inven != null && loginId.equals(inven.getInventoryMemberId())) {
            inven.setInventoryEquipped("N");
            inventoryDao.update(inven);
        }
    }

    private void unequipByType(String memberId, String type) {
        List<InventoryDto> list = inventoryDao.selectListByMemberId(memberId);
        for (InventoryDto dto : list) {
            if (type.equals(dto.getPointItemType()) && "Y".equals(dto.getInventoryEquipped())) {
                dto.setInventoryEquipped("N");
                inventoryDao.update(dto);
            }
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
    public void cancelItem(String loginId, long inventoryNo) {
        InventoryDto inven = inventoryDao.selectOne(inventoryNo);
        PointItemStoreDto item = pointItemDao.selectOneNumber(inven.getInventoryItemNo());
        addPoint(loginId, (int)item.getPointItemPrice(), "GET");
        item.setPointItemStock(item.getPointItemStock() + 1);
        pointItemDao.update(item);
        decreaseInventoryOrDelete(inven);
    }

    // [5] 후원 및 내역 조회
    @Transactional
    public void donatePoints(String loginId, String targetId, int amount) {
        addPoint(loginId, -amount, "SEND");
        addPoint(targetId, amount, "RECEIVED");
    }

    public PointHistoryPageVO getHistoryList(String loginId, int page, String type) {
        int size = 10;
        int startRow = (page - 1) * size + 1;
        int endRow = page * size;
        return PointHistoryPageVO.builder()
                .list(pointHistoryDao.selectListByMemberIdPaging(loginId, startRow, endRow, type))
                .totalPage((pointHistoryDao.countHistory(loginId, type) + size - 1) / size)
                .currentPage(page)
                .build();
    }

    // [6] 위시리스트
    @Transactional
    public boolean toggleWish(String loginId, long itemNo) {
        PointItemWishVO vo = PointItemWishVO.builder().memberId(loginId).itemNo(itemNo).build();
        if (pointWishlistDao.checkWish(vo) > 0) { pointWishlistDao.delete(vo); return false; }
        else { pointWishlistDao.insert(vo); return true; }
    }
    public List<Long> getMyWishItemNos(String id) { return pointWishlistDao.selectMyWishItemNos(id); }
    public List<PointWishlistDto> getMyWishlist(String id) { return pointWishlistDao.selectMyWishlist(id); }
    @Transactional public void deleteWish(String id, long no) { pointWishlistDao.delete(PointItemWishVO.builder().memberId(id).itemNo(no).build()); }

    // [7] 룰렛 및 정보 조회
    @Transactional
    public int playRoulette(String loginId) {
        InventoryDto ticket = inventoryDao.selectListByMemberId(loginId).stream()
                .filter(i -> "RANDOM_ROULETTE".equals(i.getPointItemType())).findFirst().orElseThrow();
        int targetIndex = (int)(Math.random() * 6);
        int reward = (targetIndex == 4) ? 2000 : (targetIndex == 0) ? 1000 : 0;
        decreaseInventoryOrDelete(ticket);
        if (reward > 0) addPoint(loginId, reward, "GET");
        dailyQuestService.questProgress(loginId, "ROULETTE");
        return targetIndex;
    }

    public MemberPointVO getMyPointInfo(String id) {
        MemberDto m = memberDao.selectMap(id);
        if (m == null) return null;

        // 아이콘은 이미지가 필요하므로 Src 유지, 나머지는 Style(Content) 조회
        String iconSrc = memberIconDao.selectEquippedIconSrc(id);
        String frameStyle = memberIconDao.selectEquippedFrameStyle(id); // "frame-gold"
        String bgStyle = memberIconDao.selectEquippedBgStyle(id);       // "bg-ice"
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
    // 관리자 기능
    @Transactional public void addItem(String id, PointItemStoreDto d) { pointItemDao.insert(d); }
    @Transactional public void editItem(String id, PointItemStoreDto d) { pointItemDao.update(d); }
    @Transactional public void deleteItem(String id, long no) { pointItemDao.delete(no); }
    @Transactional public void discardItem(String id, long no) { inventoryDao.delete(no); }
}