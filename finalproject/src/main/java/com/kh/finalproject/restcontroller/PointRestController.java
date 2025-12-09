package com.kh.finalproject.restcontroller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.kh.finalproject.dao.PointHistoryDao;
import com.kh.finalproject.dto.PointHistoryDto;

@RestController
@RequestMapping("/point") // ★ 포인트 공통 주소
@CrossOrigin
public class PointRestController {

    @Autowired private PointHistoryDao pointHistoryDao;

 
    @GetMapping("/history")
    public List<PointHistoryDto> myHistory(@RequestAttribute String loginId) {
        return pointHistoryDao.selectListByMemberId(loginId);
    }
}