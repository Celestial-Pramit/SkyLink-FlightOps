package com.skylink.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Value("${app.upload.base-path}")
    private String uploadBasePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadBasePath).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadPath + "/");
        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/images/");
    }
}
