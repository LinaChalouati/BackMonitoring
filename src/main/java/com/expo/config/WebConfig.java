package com.expo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://http://localhost:4200/")
                .allowedMethods("http://http://localhost:4200/")
                .allowedHeaders("http://http://localhost:4200/")
                .maxAge(3600);
    }
}
