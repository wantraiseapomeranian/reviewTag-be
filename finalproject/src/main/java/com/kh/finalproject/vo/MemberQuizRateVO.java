package com.kh.finalproject.vo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class MemberQuizRateVO {

	private String quizLogMemberId;
	private int quizContentsId;
	private String contentsTitle;
	private int correctCount;
	private int wrongCount;
	
	private double correctRate() {
		return correctCount / (correctCount+wrongCount);
	}
}
