package com.kh.finalproject.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import com.kh.finalproject.error.UnauthorizationException;
import com.kh.finalproject.service.TokenService;
import com.kh.finalproject.vo.TokenVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class MemberInterceptor implements HandlerInterceptor {

	@Autowired
	private TokenService tokenService;
	
	public boolean preHandle(HttpServletRequest request,
										HttpServletResponse response, Object handler) throws Exception {
		// 1) option요청 통과
		if(request.getMethod().equalsIgnoreCase("options")) return true;
		

		
		// 2) Authorization 헤더 검사
		try {
			String bearerToken = request.getHeader("Authorization");
			if(bearerToken == null) throw new UnauthorizationException();
			
			TokenVO tokenVO = tokenService.parse(bearerToken);
			request.setAttribute("tokenVO", tokenVO);
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			response.sendError(401);
			return false;
		}
	}
}
