package br.edu.ifsul.tcc.sportfy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // Interceptor de login
    @Autowired
    private LoginInterceptor loginInterceptor;

    // Registra interceptor e libera rotas públicas
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/login",
                        "/cadastro",
                        "/css/**",
                        "/js/**",
                        "/uploads/**"
                );
    }

    // Mapeia /uploads/** para a pasta local
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
