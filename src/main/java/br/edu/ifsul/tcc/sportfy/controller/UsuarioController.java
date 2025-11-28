package br.edu.ifsul.tcc.sportfy.controller;

import br.edu.ifsul.tcc.sportfy.model.Usuario;
import br.edu.ifsul.tcc.sportfy.model.Metrica;
import br.edu.ifsul.tcc.sportfy.model.Evento;
import br.edu.ifsul.tcc.sportfy.repository.UsuarioRepository;
import br.edu.ifsul.tcc.sportfy.repository.MetricaRepository;
import br.edu.ifsul.tcc.sportfy.service.FileStorageService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MetricaRepository metricaRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();



    // Exibe página de cadastro
    @GetMapping("/cadastro")
    public String exibirFormularioCadastro() {
        return "cadastro";
    }

    // Processa cadastro: valida senha, checa email/nome únicos e salva hash da senha
    @PostMapping("/cadastro")
    public String processarCadastro(Usuario usuario,
                                    @RequestParam("confirmeSenha") String confirmeSenha) {

        if (!usuario.getSenha().equals(confirmeSenha)) {
            return "redirect:/cadastro?erroSenha";
        }

        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return "redirect:/cadastro?erroEmail";
        }

        if (usuarioRepository.findByNome(usuario.getNome()) != null) {
            return "redirect:/cadastro?erroNome";
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuarioRepository.save(usuario);

        return "redirect:/login?cadastroSucesso";
    }



    // Exibe página de login
    @GetMapping("/login")
    public String exibirFormularioLogin() {
        return "login";
    }

    // Processa login: verifica email e senha criptografada, salva usuário na sessão
    @PostMapping("/login")
    public String processarLogin(@RequestParam String email,
                                 @RequestParam String senha,
                                 HttpSession session) {

        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario != null && passwordEncoder.matches(senha, usuario.getSenha())) {
            session.setAttribute("usuarioLogado", usuario);
            return "redirect:/";
        }

        return "redirect:/login?error";
    }

    // Faz logout limpando a sessão
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }



    // Exibe perfil; detalhes dependem de ser público ou dono do perfil
    @GetMapping("/perfil/{id}")
    public String exibirPerfilUsuario(@PathVariable Long id,
                                      Model model,
                                      HttpSession session) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return "redirect:/";
        }

        Usuario perfilUsuario = usuarioOpt.get();
        Usuario visitante = (Usuario) session.getAttribute("usuarioLogado");

        boolean podeVerDetalhes = false;

        if (!perfilUsuario.isPerfilPrivado()) {
            podeVerDetalhes = true;
        }

        if (visitante != null &&
            visitante.getId_usuario().equals(perfilUsuario.getId_usuario())) {
            podeVerDetalhes = true;
        }

        if (podeVerDetalhes) {

            List<Metrica> metricas = metricaRepository
                    .findByUsuarioOrderByDiaMetricaDesc(perfilUsuario);
            model.addAttribute("metricas", metricas);

            model.addAttribute("eventosCriados", perfilUsuario.getEventosCriados());

            // Filtra eventos inscritos que não são criados pelo próprio usuário
            List<Evento> eventosFiltrados = perfilUsuario.getEventosInscritos()
                    .stream()
                    .filter(e -> !e.getUsuarioCriador()
                                   .getId_usuario()
                                   .equals(perfilUsuario.getId_usuario()))
                    .collect(Collectors.toList());
            model.addAttribute("eventosInscritos", eventosFiltrados);
        }

        model.addAttribute("perfil", perfilUsuario);
        model.addAttribute("podeVerDetalhes", podeVerDetalhes);

        return "perfil";
    }

    // Exibe formulário de edição do próprio perfil
    @GetMapping("/perfil/editar")
    public String exibirFormularioEdicao(Model model,
                                         HttpSession session) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Optional<Usuario> usuarioOpt =
                usuarioRepository.findById(usuarioLogado.getId_usuario());

        if (usuarioOpt.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        return "editar-perfil";
    }

    // Salva alterações do perfil (bio, privacidade, foto)
    @PostMapping("/perfil/editar")
    public String processarEdicaoPerfil(@ModelAttribute Usuario usuarioForm,
                                        @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
                                        @RequestParam(value = "removerFoto", required = false) boolean removerFoto,
                                        HttpSession session,
                                        RedirectAttributes ra) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioLogado.getId_usuario());
        if (usuarioOpt.isPresent()) {

            Usuario usuarioParaSalvar = usuarioOpt.get();

            usuarioParaSalvar.setBio(usuarioForm.getBio());
            usuarioParaSalvar.setPerfilPrivado(usuarioForm.isPerfilPrivado());

            if (removerFoto) {
                usuarioParaSalvar.setFotoPerfilUrl(null);
            } else if (fotoFile != null && !fotoFile.isEmpty()) {
                String nomeArquivo = fileStorageService.store(fotoFile);
                usuarioParaSalvar.setFotoPerfilUrl(nomeArquivo);
            }

            usuarioRepository.save(usuarioParaSalvar);

            session.setAttribute("usuarioLogado", usuarioParaSalvar);

            ra.addFlashAttribute("toastMessage", "Perfil atualizado com sucesso!");
        }

        return "redirect:/perfil/" + usuarioLogado.getId_usuario();
    }
}
