package com.kh.finalproject.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PointHistoryDto {
    private long pointHistoryNo;       // 내역 번호 (PK)
    private String pointHistoryMemberId; // 회원 아이디 (FK)
    private int pointHistoryAmount;    // 변동 포인트 양 (+, -)
    private String pointHistoryReason; // 변동 사유 (구매, 충전, 이벤트 등)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Timestamp pointHistoryDate;     // 변동 시간 (DB insert시 sysdate)
    private long pointHistoryItemNo;   // 관련 아이템 번호 (상품 구매 시 기록)
}