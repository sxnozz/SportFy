package br.edu.ifsul.tcc.sportfy.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    // Executa antes do controller, para bloquear rotas sem login
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();

        // Rotas liberadas (não precisa estar logado)
        if (requestURI.equals("/login") ||
            requestURI.equals("/cadastro") ||
            requestURI.startsWith("/css/") ||
            requestURI.startsWith("/js/") ||
            requestURI.startsWith("/uploads/")) {
            return true; 
        }

        HttpSession session = request.getSession(); 

        // Se não está logado, bloqueia e redireciona
        if (session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect("/login");
            return false; 
        }

        return true; 
    }
}
