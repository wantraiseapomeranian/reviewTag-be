package com.kh.finalproject.dto;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class PointWishlistDto {
    // POINT_WITH_LIST 테이블 컬럼 매핑
    private int withListNo;         // WITH_LIST_NO
    private String withListMemberId; // WITH_LIST_MEMBER_ID
    private int withListItemNo;     // WITH_LIST_ITEM_NO
    private Timestamp withListDate;  // WITH_LIST_DATE
    
    // 조인(JOIN)을 통해 가져올 상품 정보
    private String pointItemName;
    private String pointItemSrc;    // 이미지 경로
    private int pointItemPrice;
}