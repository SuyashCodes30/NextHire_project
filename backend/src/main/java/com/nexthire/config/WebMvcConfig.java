package com.nexthire.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * WebMvcConfig — Configures static resource locations for serving the frontend directly.
 *
 * This allows http://localhost:8080/ to serve index.html directly from both:
 * 1. The classpath static directory (classpath:/static/)
 * 2. The project root workspace directory (file:../ and file:./)
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String workspaceDir = new File("..").getAbsolutePath() + File.separator;
        String currentDir = new File(".").getAbsolutePath() + File.separator;

        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "classpath:/static/",
                        "classpath:/public/",
                        "file:" + workspaceDir,
                        "file:" + currentDir
                )
                .setCachePeriod(0);
    }
}
