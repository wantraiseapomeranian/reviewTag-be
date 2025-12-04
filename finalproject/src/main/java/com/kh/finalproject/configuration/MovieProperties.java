package com.kh.finalproject.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "custom.movie")
public class MovieProperties {
	private String key;
	private String accessToken;
}
