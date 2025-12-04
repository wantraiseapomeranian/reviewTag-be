package com.kh.finalproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.finalproject.dao.QuizDao;

@Service
public class QuizService {
	
	@Autowired
	private QuizDao quizDao;
}
