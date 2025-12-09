package com.kh.finalproject.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.kh.finalproject.aop.MemberInterceptor;
import com.kh.finalproject.aop.TokenRenewalInterceptor;

@Configuration
public class InterceptorConfiguration implements WebMvcConfigurer {

    @Autowired
    private TokenRenewalInterceptor tokenRenewalInterceptor;
    @Autowired
    private MemberInterceptor memberInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        
        // 1. 로그인 검사 인터셉터 (회원 전용 기능 보호)
        registry.addInterceptor(memberInterceptor)
            .addPathPatterns(
                "/member/logout",      // 로그아웃
                "/point/**",
                "/review/**",
                "/content/**"
                // 포인트 관련 전체 (/point/history 등)
                // "/point/store/**"   // 위 /point/** 가 이미 포함하므로 생략 가능
            )
            .excludePathPatterns(
                "/point/store/"        // ★ 상품 목록 조회는 로그인 없이 허용
            );
        
        // 2. 토큰 재발급 인터셉터 (로그인 연장)
        registry.addInterceptor(tokenRenewalInterceptor)
            .addPathPatterns("/**")    // 모든 요청에 대해 토큰 검사 시도
            .excludePathPatterns(
                "/member/join",
                "/member/login",
                "/member/logout",
                "/point/store/",       // 상품 목록도 제외
                "/member/refresh"
            );
    }
}