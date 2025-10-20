// Em MvcConfig.java

package br.edu.ifsul.tcc.sportfy.controller; // ou .config

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import br.edu.ifsul.tcc.sportfy.controller.LoginInterceptor; // Importe o interceptor

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // --- INJEÇÃO DO INTERCEPTOR ---
    @Autowired
    private LoginInterceptor loginInterceptor;

    // --- REGISTRO DO INTERCEPTOR ---
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor);
    }

    // --- CÓDIGO ANTIGO (PARA EXIBIR IMAGENS) ---
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}