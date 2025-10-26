package br.edu.ifsul.tcc.sportfy.controller; // ou .config

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // Interceptor que checa se o usuário está autenticado.
    @Autowired
    private LoginInterceptor loginInterceptor;

    // Registra o interceptor para as rotas da aplicação,
    // exceto os caminhos públicos/estáticos para melhorar performance e evitar redirecionamentos indesejados.
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

    // Registra o mapeamento para servir arquivos de /uploads/** da pasta local "uploads/".
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
