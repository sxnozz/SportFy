package br.edu.ifsul.tcc.sportfy.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    // Intercepta requisições antes do controller ser chamado.
    // Objetivo: bloquear acesso a rotas que requerem autenticação.
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Pega a URL que o usuário está tentando acessar
        String requestURI = request.getRequestURI();

        // Lista de URLs que NÃO precisam de login — libera esses caminhos.
        if (requestURI.equals("/login") || 
            requestURI.equals("/cadastro") || 
            requestURI.startsWith("/css/") || 
            requestURI.startsWith("/js/") ||
            requestURI.startsWith("/uploads/")) {
            return true; // Deixa passar
        }
        
        // Pega a sessão do usuário
        HttpSession session = request.getSession();

        // Verifica se o atributo "usuarioLogado" existe na sessão
        if (session.getAttribute("usuarioLogado") == null) {
            // Se não existir, redireciona para a página de login
            response.sendRedirect("/login");
            return false; // Impede a requisição de continuar
        }
        
        // Se o usuário está logado, deixa a requisição passar
        return true;
    }
}
