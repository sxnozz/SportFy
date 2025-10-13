package br.edu.ifsul.tcc.sportfy.controller;

import br.edu.ifsul.tcc.sportfy.model.Usuario;
import br.edu.ifsul.tcc.sportfy.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession; // NOVO IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- MÉTODOS DE CADASTRO (JÁ EXISTENTES) ---
    @GetMapping("/cadastro")
    public String exibirFormularioCadastro() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String processarCadastro(Usuario usuario, @RequestParam("confirmeSenha") String confirmeSenha) {
        // ... (seu código de validação de cadastro continua aqui) ...
        if (!usuario.getSenha().equals(confirmeSenha)) {
            return "redirect:/cadastro?erroSenha";
        }
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return "redirect:/cadastro?erroEmail";
        }
        if (usuarioRepository.findByNome(usuario.getNome()) != null) {
            return "redirect:/cadastro?erroNome";
        }
        usuarioRepository.save(usuario);
        return "redirect:/login?cadastroSucesso";
    }

    // --- MÉTODOS DE LOGIN (NOVOS) ---

    @GetMapping("/login")
    public String exibirFormularioLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String processarLogin(@RequestParam String email, @RequestParam String senha, HttpSession session) {
        Usuario usuario = usuarioRepository.findByEmail(email);

        // TODO: Substituir esta verificação por Spring Security (criptografia)
        if (usuario != null && usuario.getSenha().equals(senha)) {
            // Login bem-sucedido: armazena informações do usuário na sessão
            session.setAttribute("usuarioLogado", usuario);
            return "redirect:/"; // Redireciona para a página principal
        } else {
            // Login falhou: redireciona de volta para o login com uma mensagem de erro
            return "redirect:/login?error";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalida a sessão, removendo todos os atributos
        return "redirect:/login?logout"; // Redireciona para a página de login com mensagem de logout
    }
}