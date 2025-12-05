package com.kh.finalproject.dto;

import java.sql.Date; // 또는 java.sql.Timestamp

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PointItemDto {
    private long pointItemNo;        // 상품 번호 (PK)
    private String pointItemName;    // 상품 이름
    private String pointItemType;    // 상품 종류 (예: FOOD, TICKET)
    private String pointItemContent; // 상품 설명
    private int pointItemPrice;      // 가격 (포인트)
    private int pointItemStock;      // 재고 수량
    private Date pointItemRegDate;   // 등록일
}