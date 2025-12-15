package com.kh.finalproject.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BoardDto {

	private int boardNo;
	private String boardTitle;
	private String boardWriter;
	private Long boardContentsId;
	private LocalDateTime boardWtime;
	private LocalDateTime boardEtime;
	private String boardText;
	private int boardLike;
	private int boardUnlike;
	private int boardReply;
	private String boardNotice;
	
}
