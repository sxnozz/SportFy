package br.edu.ifsul.tcc.sportfy.controller;

import br.edu.ifsul.tcc.sportfy.model.Usuario;
import br.edu.ifsul.tcc.sportfy.model.Metrica;
import br.edu.ifsul.tcc.sportfy.model.Evento;
import br.edu.ifsul.tcc.sportfy.repository.UsuarioRepository;
import br.edu.ifsul.tcc.sportfy.repository.MetricaRepository;
import br.edu.ifsul.tcc.sportfy.service.FileStorageService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MetricaRepository metricaRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // Exibe a página de cadastro
    @GetMapping("/cadastro")
    public String exibirFormularioCadastro() {
        return "cadastro";
    }

    // Processa o cadastro de um novo usuário
    @PostMapping("/cadastro")
    public String processarCadastro(Usuario usuario, @RequestParam("confirmeSenha") String confirmeSenha) {
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

    // Exibe a página de login
    @GetMapping("/login")
    public String exibirFormularioLogin() {
        return "login";
    }

    // Processa o login do usuário
    @PostMapping("/login")
    public String processarLogin(@RequestParam String email, @RequestParam String senha, HttpSession session) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario != null && usuario.getSenha().equals(senha)) {
            session.setAttribute("usuarioLogado", usuario);
            return "redirect:/";
        } else {
            return "redirect:/login?error";
        }
    }

    // Faz logout do usuário e invalida a sessão
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    // Exibe o perfil de um usuário
    @GetMapping("/perfil/{id}")
    public String exibirPerfilUsuario(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) return "redirect:/";

        Usuario perfilUsuario = usuarioOpt.get();
        Usuario visitante = (Usuario) session.getAttribute("usuarioLogado");

        boolean podeVerDetalhes = !perfilUsuario.isPerfilPrivado() ||
                                  (visitante != null && visitante.getId_usuario().equals(perfilUsuario.getId_usuario()));

        if (podeVerDetalhes) {
            List<Metrica> metricas = metricaRepository.findByUsuarioOrderByDiaMetricaDesc(perfilUsuario);
            model.addAttribute("metricas", metricas);

            model.addAttribute("eventosCriados", perfilUsuario.getEventosCriados());

            Set<Evento> eventosInscritos = perfilUsuario.getEventosInscritos();
            List<Evento> eventosFiltrados = eventosInscritos.stream()
                    .filter(evento -> !evento.getUsuarioCriador().getId_usuario().equals(perfilUsuario.getId_usuario()))
                    .collect(Collectors.toList());
            model.addAttribute("eventosInscritos", eventosFiltrados);
        }

        model.addAttribute("perfil", perfilUsuario);
        model.addAttribute("podeVerDetalhes", podeVerDetalhes);
        return "perfil";
    }

    // Exibe o formulário de edição de perfil
    @GetMapping("/perfil/editar")
    public String exibirFormularioEdicao(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioLogado.getId_usuario());
        if (usuarioOpt.isEmpty()) return "redirect:/";

        model.addAttribute("usuario", usuarioOpt.get());
        return "editar-perfil";
    }

    // Processa a edição do perfil do usuário
    @PostMapping("/perfil/editar")
    public String processarEdicaoPerfil(@ModelAttribute Usuario usuarioForm,
                                        @RequestParam(value = "fotoFile", required = false) MultipartFile fotoFile,
                                        @RequestParam(value = "removerFoto", required = false) boolean removerFoto,
                                        HttpSession session) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioLogado.getId_usuario());
        if (usuarioOpt.isPresent()) {
            Usuario usuarioParaSalvar = usuarioOpt.get();

            usuarioParaSalvar.setBio(usuarioForm.getBio());
            usuarioParaSalvar.setPerfilPrivado(usuarioForm.isPerfilPrivado());

            // Atualiza a foto do perfil de acordo com a ação do usuário
            if (removerFoto) {
                usuarioParaSalvar.setFotoPerfilUrl(null);
            } else if (fotoFile != null && !fotoFile.isEmpty()) {
                String nomeArquivo = fileStorageService.store(fotoFile);
                usuarioParaSalvar.setFotoPerfilUrl(nomeArquivo);
            }

            usuarioRepository.save(usuarioParaSalvar);
            session.setAttribute("usuarioLogado", usuarioParaSalvar);
        }

        return "redirect:/perfil/" + usuarioLogado.getId_usuario();
    }
}
