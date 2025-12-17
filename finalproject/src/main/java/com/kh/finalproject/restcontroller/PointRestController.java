package com.kh.finalproject.restcontroller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.kh.finalproject.dto.IconDto;
import com.kh.finalproject.dto.MemberIconDto;
import com.kh.finalproject.service.IconService;
import com.kh.finalproject.service.PointService;
import com.kh.finalproject.vo.IconPageVO;
import com.kh.finalproject.vo.PointDonateVO;
import com.kh.finalproject.vo.PointHistoryPageVO;
import com.kh.finalproject.vo.PointUseVO;

@RestController
@RequestMapping("/point") // 주소 매핑 정리 (/point/history, /point/icon/...)
@CrossOrigin
public class PointRestController {

    @Autowired private PointService pointService;
    @Autowired private IconService iconService; 

    // =============================================================
    // [1] 포인트 이용 내역 & 후원 (기존 기능)
    // =============================================================

    // 1. 이용 내역 조회 (페이징 + 필터링)
    // URL: GET /point/history?page=1&type=all
    @GetMapping("/history")
    public PointHistoryPageVO history(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "all") String type) {
        
        if(loginId == null) return null;
        return pointService.getHistoryList(loginId, page, type);
    }

    // 2. 포인트 선물하기
    // URL: POST /point/donate
    @PostMapping("/donate")
    public String donatePoints(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointDonateVO vo) {
        
        if (loginId == null) return "fail:로그인이 필요합니다.";
        if (loginId.equals(vo.getTargetId())) return "fail:자신에게는 보낼 수 없습니다.";
        if (vo.getAmount() <= 0) return "fail:올바른 금액을 입력하세요.";
        
        try {
            pointService.donatePoints(loginId, vo.getTargetId(), vo.getAmount());
            return "success";
        } catch(IllegalStateException e) {
            return "fail:" + e.getMessage();
        } catch(Exception e) {
            e.printStackTrace();
            return "fail:서버 오류";
        }
    }

    // =============================================================
    // [2] 아이콘 사용자 기능 (뽑기, 조회, 장착)
    // =============================================================

    // 1. 아이콘 뽑기 실행
    // URL: POST /point/icon/draw
    @PostMapping("/icon/draw")
    public IconDto drawIcon(
            @RequestAttribute(value="loginId", required=false) String loginId,
            @RequestBody PointUseVO vo) { // 인벤토리 번호(티켓) 수신
        
        if(loginId == null) throw new RuntimeException("로그인 필요");
        
        // Service로직 실행
        return iconService.drawRandomIcon(loginId, vo.getInventoryNo());
    }

    // 2. 내 아이콘 보관함 조회
    // URL: GET /point/icon/my
    @GetMapping("/icon/my")
    public List<MemberIconDto> myIcons(@RequestAttribute(value="loginId", required=false) String loginId) {
        if(loginId == null) return List.of();
        
        // DTO 필드명이 memberId, iconId로 변경된 리스트 반환
        return iconService.getMyIcons(loginId);
    }

    // 3. 아이콘 장착
    // URL: POST /point/icon/equip
    // Body: { "iconId": 5 }  <-- DTO 필드명 iconId와 일치해야 함
    @PostMapping("/icon/equip")
    public String equipIcon(
            @RequestAttribute("loginId") String loginId, 
            @RequestBody IconDto dto) { 
        try {
            // DTO의 iconId를 꺼내서 서비스 호출
            iconService.equipIcon(loginId, dto.getIconId());
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // 4. 아이콘 장착 해제 (모두 해제)
    // URL: POST /point/icon/unequip
    @PostMapping("/icon/unequip")
    public String unequipIcon(@RequestAttribute("loginId") String loginId) {
        try {
            iconService.unequipIcon(loginId);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // =============================================================
    // [3] 아이콘 관리자 기능 (목록, 등록, 수정, 삭제)
    // =============================================================
    // 1. 관리자용 아이콘 전체 목록 (페이징)
    // URL: GET /point/icon/admin/list?page=1&type=ALL
    @GetMapping("/icon/admin/list")
    public IconPageVO adminIconList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "ALL") String type) {
        
        return iconService.getIconList(page, type);
    }

    // 2. 아이콘 등록
    // URL: POST /point/icon/admin/add
    @PostMapping("/icon/admin/add")
    public String addIcon(@RequestBody IconDto dto) {
        try {
            iconService.addIcon(dto);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // 3. 아이콘 수정
    // URL: POST /point/icon/admin/edit
    @PostMapping("/icon/admin/edit")
    public String editIcon(@RequestBody IconDto dto) {
        try {
            iconService.editIcon(dto);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // 4. 아이콘 삭제
    // URL: DELETE /point/icon/admin/delete/{iconId}
    @DeleteMapping("/icon/admin/delete/{iconId}")
    public String deleteIcon(@PathVariable int iconId) {
        try {
            iconService.removeIcon(iconId);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
}