package com.kh.finalproject.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.finalproject.dao.*;
import com.kh.finalproject.dto.*;
import com.kh.finalproject.vo.PointItemWishVO;

@Service
public class PointService {

    @Autowired private PointItemDao pointItemDao;
    @Autowired private MemberDao memberDao;
    @Autowired private PointInventoryDao pointInventoryDao;
    @Autowired private PointHistoryDao pointHistoryDao;
    @Autowired private PointWishlistDao pointWishlistDao;
    // 등급 점수 변환
    private int getLevelWeight(String level) {
        if (level == null) return 0;
        switch (level) {
            case "관리자": return 99;
            case "우수회원": return 2;
            case "일반회원": return 1;
            default: return 0;
        }
    }

    // [통합 결제 로직]
    private void processTransaction(String senderId, String receiverId, long itemNo, String type) {
        // 1. 상품 & 재고 체크
        PointItemDto item = pointItemDao.selectOneNumber(itemNo);
        if (item == null) throw new RuntimeException("상품 없음");
        if (item.getPointItemStock() <= 0) throw new RuntimeException("품절");


        if (item.getPointItemUniques() == 1) { // 1이면 '1회 한정' 아이템
            // 받는 사람(receiverId)이 이미 가지고 있는지 검사
            int count = pointInventoryDao.selectCountMyItem(receiverId, itemNo);
            if (count > 0) {
                throw new RuntimeException("이미 보유하고 있는 아이템입니다. (중복 구매 불가)");
            }
        }
        // ==========================================================

        // 2. 구매자(Sender) DB 정보 조회 
        MemberDto sender = memberDao.selectOne(senderId);

        // 3. 등급 체크
        int userScore = getLevelWeight(sender.getMemberLevel());
        int reqScore = getLevelWeight(item.getPointItemReqLevel());

        if (!"관리자".equals(sender.getMemberLevel()) && userScore < reqScore) {
            throw new RuntimeException("등급 부족 (" + item.getPointItemReqLevel() + " 이상)");
        }

        // 4. 포인트 체크
        if (sender.getMemberPoint() < item.getPointItemPrice()) {
            throw new RuntimeException("포인트 부족");
        }

        // 5. 결제 진행 (포인트 차감, 재고 차감)
        sender.setMemberPoint(sender.getMemberPoint() - item.getPointItemPrice());
        memberDao.updatePoint(sender);

        item.setPointItemStock(item.getPointItemStock() - 1);
        pointItemDao.update(item);

        // 6. 인벤토리 지급
        PointInventoryDto inven = new PointInventoryDto();
        inven.setPointInventoryMemberId(receiverId);
        inven.setPointInventoryItemNo((int)itemNo);
        inven.setPointInventoryItemAmount(1);
        inven.setPointInventoryItemType(item.getPointItemType());
        pointInventoryDao.insert(inven);

        // 7. 내역 기록
        PointHistoryDto history = new PointHistoryDto();
        history.setPointHistoryMemberId(senderId);
        history.setPointHistoryAmount(-item.getPointItemPrice());
        history.setPointHistoryReason(type + ": " + item.getPointItemName());
        history.setPointHistoryItemNo((int)itemNo);
        pointHistoryDao.insert(history);
    }

    // [구매]
    @Transactional
    public void purchaseItem(String loginId, long itemNo) {
        processTransaction(loginId, loginId, itemNo, "구매");
    }

    // [선물]
    @Transactional
    public void giftItem(String senderId, String targetId, long itemNo) {
        if (senderId.equals(targetId)) throw new RuntimeException("본인 선물 불가");
        if (memberDao.selectOne(targetId) == null) throw new RuntimeException("회원 없음");
        processTransaction(senderId, targetId, itemNo, "선물(" + targetId + ")");
    }

    // [취소/환불]
    @Transactional
    public void cancelItem(String loginId, long inventoryNo) {
        PointInventoryDto inven = pointInventoryDao.selectOneNumber((int)inventoryNo);
        if (!inven.getPointInventoryMemberId().equals(loginId)) throw new RuntimeException("권한 없음");
        
        PointItemDto item = pointItemDao.selectOneNumber(inven.getPointInventoryItemNo());
        MemberDto member = memberDao.selectOne(loginId);

        // 환불 및 복구
        member.setMemberPoint(member.getMemberPoint() + item.getPointItemPrice());
        memberDao.updatePoint(member);
        
        item.setPointItemStock(item.getPointItemStock() + 1);
        pointItemDao.update(item);
        
        pointInventoryDao.delete((int)inventoryNo);

        // 기록
        PointHistoryDto history = new PointHistoryDto();
        history.setPointHistoryMemberId(loginId);
        history.setPointHistoryAmount(item.getPointItemPrice());
        history.setPointHistoryReason("취소: " + item.getPointItemName());
        history.setPointHistoryItemNo(item.getPointItemNo());
        pointHistoryDao.insert(history);
    }
    
    // [관리자] 상품 등록
    @Transactional
    public void addItem(String loginId, PointItemDto itemDto) {
        MemberDto member = memberDao.selectOne(loginId);
        if (member == null || !member.getMemberLevel().equals("관리자")) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }
        pointItemDao.insert(itemDto);
    }
 // [관리자] 상품 수정
    @Transactional
    public void editItem(String loginId, PointItemDto itemDto) {
        // 1. 관리자 체크
        MemberDto member = memberDao.selectOne(loginId);
        if (member == null || !member.getMemberLevel().equals("관리자")) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }
        // 2. 수정 실행
        pointItemDao.update(itemDto);
    }
 // [관리자] 상품 삭제
    @Transactional
    public void deleteItem(String loginId, int itemNo) {
        // 1. 관리자 체크
        MemberDto member = memberDao.selectOne(loginId);
        if (member == null || !member.getMemberLevel().equals("관리자")) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }
        // 2. 삭제 실행
        pointItemDao.delete(itemNo);
    }
    //인벤토리 아이템폐기
    @Transactional
    public void discardItem(String loginId, int inventoryNo) {
        // 1. 내 아이템인지 확인
        PointInventoryDto inven = pointInventoryDao.selectOneNumber(inventoryNo);
        if (inven == null) throw new RuntimeException("아이템이 없습니다.");
        if (!inven.getPointInventoryMemberId().equals(loginId)) throw new RuntimeException("본인 아이템만 삭제할 수 있습니다.");

        // 아이템 정보 조회 (로그용)
        PointItemDto item = pointItemDao.selectOneNumber(inven.getPointInventoryItemNo());

        // 2. 삭제 실행
        pointInventoryDao.delete(inventoryNo);

        // 3. 내역 기록 (변동액 0)
        PointHistoryDto history = new PointHistoryDto();
        history.setPointHistoryMemberId(loginId);
        history.setPointHistoryAmount(0); // 0원 처리
        history.setPointHistoryReason("아이템 삭제(폐기): " + item.getPointItemName());
        history.setPointHistoryItemNo(item.getPointItemNo());
        pointHistoryDao.insert(history);
    }

    @Transactional
    public void useItem(String loginId, int inventoryNo, String extraValue) {
        // 1. 내 아이템인지 확인
        PointInventoryDto inven = pointInventoryDao.selectOneNumber(inventoryNo);
        if (inven == null || !inven.getPointInventoryMemberId().equals(loginId)) {
            throw new RuntimeException("아이템이 존재하지 않거나 권한이 없습니다.");
        }

        // 2. 아이템 정보 조회
        PointItemDto item = pointItemDao.selectOneNumber(inven.getPointInventoryItemNo());
        String type = item.getPointItemType();

        // 3. ★ 유형별 기능 실행 (여기서 기능을 확장하면 됩니다)
        switch (type) {
            case "CHANGE_NICK": // 닉네임 변경권
                if (extraValue == null || extraValue.trim().isEmpty()) {
                    throw new RuntimeException("변경할 닉네임을 입력해주세요.");
                }
                // 회원 닉네임 변경 (MemberDao에 updateNickname 메소드 필요)
                // memberDao.updateNickname(loginId, extraValue);
                
                // (간단하게 기존 update 활용 예시)
                MemberDto member = memberDao.selectOne(loginId);
                member.setMemberNickname(extraValue);
                memberDao.updateNickname(member); 
                break;

            case "LEVEL_UP": // 레벨업 아이템 (예시)
                // memberDao.levelUp(loginId);
                break;
                
            // ... 다른 아이템들 ...
            
            default:
                throw new RuntimeException("사용할 수 없는 아이템입니다. (관리자 문의)");
        }

        // 4. 아이템 소모 (삭제)
        pointInventoryDao.delete(inventoryNo);

        // 5. 내역 기록
        PointHistoryDto history = new PointHistoryDto();
        history.setPointHistoryMemberId(loginId);
        history.setPointHistoryAmount(0);
        history.setPointHistoryReason("아이템 사용: " + item.getPointItemName());
        history.setPointHistoryItemNo(item.getPointItemNo());
        pointHistoryDao.insert(history);
    }
    @Transactional
    public boolean toggleWish(String loginId, int itemNo) {
        // DAO에 전달할 VO 생성
        PointItemWishVO vo = PointItemWishVO.builder()
                            .memberId(loginId)
                            .itemNo(itemNo)
                            .build();

        // 찜 여부 확인
        int count = pointWishlistDao.checkWish(vo); 
        
        if (count > 0) {
            pointWishlistDao.delete(vo); // 이미 찜했으면 삭제
            return false;
        } else {
            pointWishlistDao.insert(vo); // 찜하지 않았으면 추가
            return true;
        }
    }

    // 내 찜 아이템 번호 리스트 조회
    public List<Integer> getMyWishItemNos(String loginId) {
        return pointWishlistDao.selectMyWishItemNos(loginId);
    }
 // 내 찜 목록 전체 조회
    public List<PointWishlistDto> getMyWishlist(String loginId) {
        return pointWishlistDao.selectMyWishlist(loginId);
    }
    //찜목록 삭제
    @Transactional
    public void deleteWish(String loginId, int itemNo) {
        // ★ [디버깅용 로그 추가] 콘솔창에 이 값이 찍히는지 확인하세요!
        System.out.println(">>> 찜 삭제 요청 도착!");
        System.out.println("요청자(ID): " + loginId);
        System.out.println("지울 상품번호(ItemNo): " + itemNo);

        // VO 생성
        PointItemWishVO vo = PointItemWishVO.builder()
                            .memberId(loginId)
                            .itemNo(itemNo)
                            .build();
        
        // 삭제 실행 (이게 실행돼도 조건 안맞으면 0개 삭제됨)
        pointWishlistDao.delete(vo);
        
        System.out.println(">>> 삭제 쿼리 실행 완료");
    }
    
    @Transactional
    public void addAttendancePoint(String loginId, int amount, String memo) {

        // 1) 현재 회원 정보 조회
        MemberDto member = memberDao.selectOne(loginId);
        if (member == null) {
            throw new IllegalStateException("회원이 존재하지 않습니다: " + loginId);
        }

        // 2) 현재 포인트 + 지급 포인트 계산
        int newPoint = member.getMemberPoint() + amount;
        member.setMemberPoint(newPoint);

        // 3) 업데이트
        boolean result = memberDao.updatePoint(member);
        if (!result) {
            throw new IllegalStateException("포인트 업데이트 실패: " + loginId);
        }

        // 4) 포인트 히스토리 기록 (있다면)
        PointHistoryDto dto = PointHistoryDto.builder()
                .pointHistoryMemberId(loginId)
                .pointHistoryAmount(amount)
                .pointHistoryReason(memo)
                .build();
        pointHistoryDao.insertHistory(dto);

        System.out.println(loginId + "님에게 " + amount + "포인트 지급 완료 ▶ 현재 포인트: " + newPoint);
    }
}
