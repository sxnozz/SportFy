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

    // encoder para criptografar/verificar senha
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /* -------------------------------------------------
     * CADASTRO
     * ------------------------------------------------- */

    /** Exibe página de cadastro. */
    @GetMapping("/cadastro")
    public String exibirFormularioCadastro() {
        return "cadastro";
    }

    /**
     * Processa o cadastro:
     * - valida senha igual à confirmação
     * - garante e-mail e nome únicos
     * - salva senha criptografada com BCrypt
     * - redireciona para /login com flag de sucesso
     */
    @PostMapping("/cadastro")
    public String processarCadastro(Usuario usuario,
                                    @RequestParam("confirmeSenha") String confirmeSenha) {

        // senhas diferentes → erro
        if (!usuario.getSenha().equals(confirmeSenha)) {
            return "redirect:/cadastro?erroSenha";
        }

        // e-mail já está no sistema → erro
        if (usuarioRepository.findByEmail(usuario.getEmail()) != null) {
            return "redirect:/cadastro?erroEmail";
        }

        // nome de usuário já está em uso → erro
        if (usuarioRepository.findByNome(usuario.getNome()) != null) {
            return "redirect:/cadastro?erroNome";
        }

        // criptografa antes de salvar
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        usuarioRepository.save(usuario);

        return "redirect:/login?cadastroSucesso";
    }

    /* -------------------------------------------------
     * LOGIN / LOGOUT
     * ------------------------------------------------- */

    /** Exibe página de login. */
    @GetMapping("/login")
    public String exibirFormularioLogin() {
        return "login";
    }

    /**
     * Faz login manualmente (sem Spring Security completo):
     * - busca usuário por email
     * - compara senha digitada com hash salvo usando BCrypt
     * - se ok, guarda o usuário na sessão.
     */
    @PostMapping("/login")
    public String processarLogin(@RequestParam String email,
                                 @RequestParam String senha,
                                 HttpSession session) {

        Usuario usuario = usuarioRepository.findByEmail(email);

        // usuário existe E senha confere com o hash?
        if (usuario != null && passwordEncoder.matches(senha, usuario.getSenha())) {

            // login ok → salva usuário na sessão
            session.setAttribute("usuarioLogado", usuario);

            return "redirect:/";
        } else {
            // login falhou
            return "redirect:/login?error";
        }
    }

    /** Faz logout limpando a sessão. */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }

    /* -------------------------------------------------
     * PERFIL
     * ------------------------------------------------- */

    /**
     * Exibe o perfil público (ou privado).
     * Regras:
     * - se perfil for público → qualquer um pode ver
     * - se perfil for privado → só o dono pode ver detalhes
     * Sempre renderiza pelo menos o cabeçalho com nome/foto/bio.
     */
    @GetMapping("/perfil/{id}")
    public String exibirPerfilUsuario(@PathVariable Long id,
                                      Model model,
                                      HttpSession session) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return "redirect:/"; // perfil não existe
        }

        Usuario perfilUsuario = usuarioOpt.get(); // usuário dono do perfil
        Usuario visitante = (Usuario) session.getAttribute("usuarioLogado"); // quem está olhando

        boolean podeVerDetalhes = false;

        // perfil público → todo mundo pode ver detalhes
        if (!perfilUsuario.isPerfilPrivado()) {
            podeVerDetalhes = true;
        }

        // perfil privado → só o próprio dono pode ver detalhes
        if (visitante != null &&
            visitante.getId_usuario().equals(perfilUsuario.getId_usuario())) {
            podeVerDetalhes = true;
        }

        // se tem permissão pra ver detalhes, carrega métricas e eventos
        if (podeVerDetalhes) {

            // métricas do usuário (ordenadas por data desc)
            List<Metrica> metricas = metricaRepository
                    .findByUsuarioOrderByDiaMetricaDesc(perfilUsuario);
            model.addAttribute("metricas", metricas);

            // eventos criados
            model.addAttribute("eventosCriados", perfilUsuario.getEventosCriados());

            // eventos em que está inscrito, mas que NÃO são dele mesmo
            Set<Evento> eventosInscritos = perfilUsuario.getEventosInscritos();
            List<Evento> eventosFiltrados = eventosInscritos.stream()
                    .filter(evento ->
                            !evento.getUsuarioCriador()
                                   .getId_usuario()
                                   .equals(perfilUsuario.getId_usuario()))
                    .collect(Collectors.toList());
            model.addAttribute("eventosInscritos", eventosFiltrados);
        }

        model.addAttribute("perfil", perfilUsuario);
        model.addAttribute("podeVerDetalhes", podeVerDetalhes);

        return "perfil";
    }

    /**
     * Exibe o formulário de edição do próprio perfil.
     * Só o usuário logado tem acesso a essa página.
     */
    @GetMapping("/perfil/editar")
    public String exibirFormularioEdicao(Model model,
                                         HttpSession session) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // busca versão atualizada do banco (evita usar objeto desatualizado da sessão)
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioLogado.getId_usuario());
        if (usuarioOpt.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        return "editar-perfil";
    }

    /**
     * Salva alterações de perfil (bio, privacidade, foto).
     *
     * NOVO: agora essa ação também dispara um toast de sucesso.
     *
     * Regras:
     * - Se "removerFoto" vier marcado → fotoPerfilUrl = null.
     * - Senão, se veio arquivo novo → salva no disco e atualiza fotoPerfilUrl.
     * - Se não veio nada → mantém a foto anterior.
     *
     * Depois de salvar:
     * - atualiza o objeto da sessão pra refletir mudanças imediatamente
     * - redireciona para /perfil/{id} com flash "Perfil atualizado com sucesso!"
     */
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

            // atualiza bio e privacidade
            usuarioParaSalvar.setBio(usuarioForm.getBio());
            usuarioParaSalvar.setPerfilPrivado(usuarioForm.isPerfilPrivado());

            // trata foto
            if (removerFoto) {
                // usuário marcou "remover foto"
                usuarioParaSalvar.setFotoPerfilUrl(null);

            } else if (fotoFile != null && !fotoFile.isEmpty()) {
                // usuário enviou uma nova foto → salva e atualiza o nome do arquivo
                String nomeArquivo = fileStorageService.store(fotoFile);
                usuarioParaSalvar.setFotoPerfilUrl(nomeArquivo);
            }
            // caso contrário, mantém a foto atual

            // salva alterações no banco
            usuarioRepository.save(usuarioParaSalvar);

            // atualiza sessão pra refletir já na navbar/header
            session.setAttribute("usuarioLogado", usuarioParaSalvar);

            // >>> NOVO: configura mensagem de sucesso pro toast
            ra.addFlashAttribute("toastMessage", "Perfil atualizado com sucesso!");
        }

        return "redirect:/perfil/" + usuarioLogado.getId_usuario();
    }
}
