package com.kh.finalproject.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data @AllArgsConstructor@Builder
@NoArgsConstructor
public class PointGiftVO {
 private int itemNo;
 public String TargetId;
 
}
