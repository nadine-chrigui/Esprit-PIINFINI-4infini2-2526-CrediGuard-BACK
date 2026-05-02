package tn.esprit.pi_back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cette configuration est désactivée car le CORS est désormais géré 
 * centralement dans SecurityConfig.java pour éviter les conflits.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Vide pour éviter les conflits avec Spring Security
}