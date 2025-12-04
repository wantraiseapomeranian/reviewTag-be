package com.kh.finalproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.finalproject.dao.QuizLogDao;

@Service
public class QuizLogService {

	@Autowired
	private QuizLogDao quizLogDao;
}
