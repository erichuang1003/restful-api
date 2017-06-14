package com.kyy.demo.config;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.kyy.demo.annotation.CurrentUserIdMethodArgumentResolver;
import com.kyy.demo.interceptor.AuthorizationInterceptor;

@SpringBootApplication(scanBasePackages = "com.kyy.demo")
@PropertySource("classpath:${spring.profiles.active:dev}/app.properties")
public class AppConfig {

	@Bean
	public CurrentUserIdMethodArgumentResolver currentUserIdMethodArgumentResolver() {
		return new CurrentUserIdMethodArgumentResolver();
	}

	@Bean
	public AuthorizationInterceptor authorizationInterceptor() {
		return new AuthorizationInterceptor();
	}

	@Bean
	public WebMvcConfigurer webMvcConfigurer() {
		return new WebMvcConfigurerAdapter() {

			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				registry.addInterceptor(authorizationInterceptor()).excludePathPatterns("/swagger-resources/**")
						.excludePathPatterns("/v2/**");
			}

			@Override
			public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
				argumentResolvers.add(currentUserIdMethodArgumentResolver());
			}

		};
	}

	public static void main(String[] args) {
		SpringApplication.run(AppConfig.class, args);
	}
}
