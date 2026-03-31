package com.appg.influencerdemo.common.config;

import com.appg.influencerdemo.instagramlogin.config.InstagramLoginProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties({
        DemoCorsProperties.class,
        InstagramLoginProperties.class
})
public class WebConfig implements WebMvcConfigurer {

    private final DemoCorsProperties demoCorsProperties;

    public WebConfig(DemoCorsProperties demoCorsProperties) {
        this.demoCorsProperties = demoCorsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(demoCorsProperties.getAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
