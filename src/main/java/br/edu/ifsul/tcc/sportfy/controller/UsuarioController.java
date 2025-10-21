package br.edu.ifsul.tcc.sportfy.controller;

import br.edu.ifsul.tcc.sportfy.model.Usuario;
import br.edu.ifsul.tcc.sportfy.repository.MetricaRepository;
import br.edu.ifsul.tcc.sportfy.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession; // NOVO IMPORT
import br.edu.ifsul.tcc.sportfy.service.FileStorageService; // Vamos criar este serviço
import org.springframework.validation.BindingResult; // Para validação
import br.edu.ifsul.tcc.sportfy.model.Evento;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable; // Adicione este import
import br.edu.ifsul.tcc.sportfy.repository.MetricaRepository; // Adicione este import
import br.edu.ifsul.tcc.sportfy.model.Metrica; // Adicione este import


@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired // <-- ADICIONE ESTA INJEÇÃO
    private MetricaRepository metricaRepository;

    @Autowired
    private FileStorageService fileStorageService;

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


  @GetMapping("/perfil/{id}")
public String exibirPerfilUsuario(@PathVariable Long id, Model model, HttpSession session) {
    
    Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

    if (usuarioOpt.isEmpty()) {
        return "redirect:/"; // Usuário não existe
    }

    Usuario perfilUsuario = usuarioOpt.get();
    Usuario visitante = (Usuario) session.getAttribute("usuarioLogado");

    // --- LÓGICA DE PRIVACIDADE ---
    // Por padrão, ninguém pode ver os detalhes.
    boolean podeVerDetalhes = false; 

    // Condição 1: O perfil NÃO é privado. Todos podem ver.
    if (!perfilUsuario.isPerfilPrivado()) {
        podeVerDetalhes = true;
    }
    
    // Condição 2: O perfil É privado, mas o visitante é o DONO do perfil.
    // O dono sempre pode ver suas próprias coisas.
    if (visitante != null && visitante.getId_usuario().equals(perfilUsuario.getId_usuario())) {
        podeVerDetalhes = true;
    }

    // Se podeVerDetalhes for true, buscamos as informações e as adicionamos ao model.
    if (podeVerDetalhes) {
        // Busca as métricas
        List<Metrica> metricas = metricaRepository.findByUsuarioOrderByDiaMetricaDesc(perfilUsuario);
        model.addAttribute("metricas", metricas);

        // Busca os eventos que o usuário criou
        model.addAttribute("eventosCriados", perfilUsuario.getEventosCriados());

        // Busca os eventos em que o usuário está inscrito e filtra
        Set<Evento> eventosInscritos = perfilUsuario.getEventosInscritos();
        List<Evento> eventosFiltrados = eventosInscritos.stream()
                .filter(evento -> !evento.getUsuarioCriador().getId_usuario().equals(perfilUsuario.getId_usuario()))
                .collect(Collectors.toList());
        model.addAttribute("eventosInscritos", eventosFiltrados);
    }
    
    model.addAttribute("perfil", perfilUsuario);
    model.addAttribute("podeVerDetalhes", podeVerDetalhes); // Envia a permissão para o HTML

    return "perfil";
}

// --- NOVOS MÉTODOS PARA EDITAR PERFIL ---

    // 1. MÉTODO GET: MOSTRA O FORMULÁRIO DE EDIÇÃO
    @GetMapping("/perfil/editar")
    public String exibirFormularioEdicao(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        
        // Busca a versão mais atual do usuário no banco
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioLogado.getId_usuario());
        if (usuarioOpt.isEmpty()) {
            return "redirect:/";
        }

        model.addAttribute("usuario", usuarioOpt.get());
        return "editar-perfil"; // Nome da nova página HTML
    }

    // 2. MÉTODO POST: SALVA AS ALTERAÇÕES DO PERFIL
    @PostMapping("/perfil/editar")
    public String processarEdicaoPerfil(@ModelAttribute Usuario usuarioForm, 
                                        @RequestParam("fotoFile") MultipartFile fotoFile,
                                        HttpSession session) {
        
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioLogado.getId_usuario());
        if (usuarioOpt.isPresent()) {
            Usuario usuarioParaSalvar = usuarioOpt.get();
            
            // Atualiza a bio e a preferência de privacidade
            usuarioParaSalvar.setBio(usuarioForm.getBio());
            usuarioParaSalvar.setPerfilPrivado(usuarioForm.isPerfilPrivado());

            // Se um novo arquivo de foto foi enviado...
            if (!fotoFile.isEmpty()) {
                // ...salva o arquivo e obtém o nome gerado
                String nomeArquivo = fileStorageService.store(fotoFile);
                // ...atualiza o campo no usuário
                usuarioParaSalvar.setFotoPerfilUrl(nomeArquivo);
            }

            usuarioRepository.save(usuarioParaSalvar);
            // Atualiza o objeto na sessão para refletir as mudanças imediatamente no header
            session.setAttribute("usuarioLogado", usuarioParaSalvar);
        }

        // Redireciona de volta para a página de perfil
        return "redirect:/perfil/" + usuarioLogado.getId_usuario();
    }
}
