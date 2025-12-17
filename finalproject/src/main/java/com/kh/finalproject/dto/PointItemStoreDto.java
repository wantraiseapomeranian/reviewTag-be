package com.kh.finalproject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class PointItemStoreDto {
    private long pointItemNo;
    private String pointItemName;
    private String pointItemType;
    private String pointItemContent;
    private long pointItemPrice;
    private long pointItemStock;
    private String pointItemSrc;
    private String pointItemReqLevel;
    private Timestamp pointItemRegDate;
    // 1이면 한정구매, 0이면 무제한이라고 가정
    private int pointItemIsLimitedPurchase; 
}