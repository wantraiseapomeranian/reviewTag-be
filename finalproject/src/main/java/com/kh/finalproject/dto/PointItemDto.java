package com.kh.finalproject.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointItemDto {
    private int pointItemNo;
    private String pointItemName;
    private String pointItemType;
    private String pointItemContent;
    private int pointItemPrice;
    private int pointItemStock;
    private String pointItemSrc;      // 이미지
    private String pointItemReqLevel; // ★ 필수 등급 (일반회원/우수회원/관리자)
    private Timestamp pointItemRegDate;
    private int pointItemUniques;
}