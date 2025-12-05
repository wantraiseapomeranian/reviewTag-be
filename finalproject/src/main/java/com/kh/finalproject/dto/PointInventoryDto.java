package com.kh.finalproject.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PointInventoryDto {
    private long pointInventoryNo;        // 인벤토리 고유 번호 (PK)
    private String pointInventoryMemberId; // 소유자 아이디 (FK)
    private long pointInventoryItemNo;    // 상품 번호 (FK, 어떤 아이템인지)
    private int pointInventoryItemAmount; // 보유 수량
    private String pointInventoryItemType;// 아이템 타입 (검색 편의성 등을 위해 저장)
}