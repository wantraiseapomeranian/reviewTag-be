package com.kh.finalproject.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PointInventoryDto {
    private int pointInventoryNo;
    private String pointInventoryMemberId;
    private int pointInventoryItemNo;
    private int pointInventoryItemAmount;
    private String pointInventoryItemType;
    private Timestamp pointInventoryPurchaseDate;
    
    // 조인용 필드 (화면 표시용)
    private String pointItemName;
    private String pointItemSrc;
}

