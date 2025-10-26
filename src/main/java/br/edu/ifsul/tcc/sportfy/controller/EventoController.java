package br.edu.ifsul.tcc.sportfy.controller;

import br.edu.ifsul.tcc.sportfy.model.Evento;
import br.edu.ifsul.tcc.sportfy.model.Usuario;
import br.edu.ifsul.tcc.sportfy.repository.EventoRepository;
import br.edu.ifsul.tcc.sportfy.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class EventoController {

    // Repositório para operações CRUD com eventos.
    @Autowired
    private EventoRepository eventoRepository;

    // Repositório para operações com usuários.
    @Autowired
    private UsuarioRepository usuarioRepository;

    // --- PÁGINA PRINCIPAL ---
    // Exibe a página inicial (home) com lista de eventos.
    // Se 'modalidade' for fornecida, filtra por modalidade.
    @GetMapping("/")
    public String exibirPaginaPrincipal(@RequestParam(name = "modalidade", required = false) String modalidade, Model model) {
        List<Evento> eventos;
        if (modalidade != null && !modalidade.isEmpty()) {
            eventos = eventoRepository.findByModalidadeEvento(modalidade);
        } else {
            eventos = eventoRepository.findAll();
        }
        model.addAttribute("eventos", eventos);
        model.addAttribute("activePage", "eventos");
        model.addAttribute("selectedModalidade", modalidade);
        return "home";
    }

    // --- MÉTODOS PARA CRIAÇÃO DE EVENTO ---
    // Mostra formulário para criar novo evento (modelo Evento vazio no model).
    @GetMapping("/eventos/novo")
    public String exibirFormularioNovoEvento(Model model) {
        model.addAttribute("evento", new Evento());
        model.addAttribute("activePage", "meusEventos");
        return "novo-evento";
    }

    // Processa POST do formulário de novo evento.
    // Define o criador como usuário logado e grava horário de postagem.
    @PostMapping("/eventos/novo")
    public String processarNovoEvento(@ModelAttribute Evento evento, HttpSession session) {
        // A verificação de data não é necessária aqui, pois é um novo evento.
        // O código original permanece.
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }
        evento.setUsuarioCriador(usuarioLogado);
        evento.setHorario_de_postagem(LocalDateTime.now());
        eventoRepository.save(evento);
        return "redirect:/";
    }

    // Inscreve o usuário logado como participante do evento.
    // Bloqueia inscrição se o evento já passou.
    @PostMapping("/evento/{id}/participar")
    public String participarDoEvento(@PathVariable Long id, HttpSession session) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();

            // VERIFICAÇÃO DE DATA
            if (evento.getDataHoraEvento().isBefore(LocalDateTime.now())) {
                return "redirect:/evento/" + id; // Apenas redireciona de volta
            }

            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            evento.getParticipantes().add(usuarioLogado);
            eventoRepository.save(evento);
        }
        return "redirect:/evento/" + id;
    }

    // Exibe a página de detalhes do evento.
    // 'from' permite controlar qual menu/página estava ativa ao navegar.
    @GetMapping("/evento/{id}")
    public String exibirDetalhesDoEvento(@PathVariable Long id,
                                         @RequestParam(name = "from", required = false, defaultValue = "eventos") String fromPage,
                                         Model model) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);
        if (eventoOpt.isPresent()) {
            model.addAttribute("evento", eventoOpt.get());
            model.addAttribute("activePage", fromPage);
            return "evento-detalhes";
        } else {
            return "redirect:/";
        }
    }

    // Mostra os eventos criados pelo usuário logado.
    @GetMapping("/meus-eventos")
    public String exibirMeusEventos(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        List<Evento> eventos = eventoRepository.findByUsuarioCriador(usuarioLogado);
        model.addAttribute("eventos", eventos);
        model.addAttribute("activePage", "meusEventos");
        return "meus-eventos";
    }

    // Exclui um evento (somente se o evento ainda não passou e o usuário for o criador).
    @PostMapping("/eventos/excluir/{id}")
    public String excluirEvento(@PathVariable Long id, HttpSession session) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

            // VERIFICAÇÃO DE DATA
            if (evento.getDataHoraEvento().isBefore(LocalDateTime.now())) {
                return "redirect:/meus-eventos"; // Impede exclusão de evento passado
            }

            if (evento.getUsuarioCriador().getId_usuario().equals(usuarioLogado.getId_usuario())) {
                eventoRepository.deleteById(id);
            }
        }
        return "redirect:/meus-eventos";
    }

    // Exibe formulário de edição para um evento (somente se o evento não passou e o usuário for o criador).
    @GetMapping("/eventos/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable Long id, Model model, HttpSession session) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

            // VERIFICAÇÃO DE DATA E PROPRIEDADE
            if (evento.getDataHoraEvento().isBefore(LocalDateTime.now()) || !evento.getUsuarioCriador().getId_usuario().equals(usuarioLogado.getId_usuario())) {
                return "redirect:/evento/" + id; // Não pode editar se o evento passou ou não é o dono
            }

            model.addAttribute("evento", evento);
            model.addAttribute("activePage", "meusEventos");
            return "editar-evento";
        }
        return "redirect:/";
    }

    // Processa envio do formulário de edição e atualiza os campos permitidos do evento.
    @PostMapping("/eventos/editar/{id}")
    public String processarEdicaoEvento(@PathVariable Long id, @ModelAttribute Evento eventoAtualizado, HttpSession session) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);
        if (eventoOpt.isPresent()) {
            Evento eventoOriginal = eventoOpt.get();
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

            // VERIFICAÇÃO DE DATA E PROPRIEDADE
            if (eventoOriginal.getDataHoraEvento().isBefore(LocalDateTime.now()) || !eventoOriginal.getUsuarioCriador().getId_usuario().equals(usuarioLogado.getId_usuario())) {
                return "redirect:/evento/" + id;
            }

            eventoOriginal.setModalidadeEvento(eventoAtualizado.getModalidadeEvento());
            eventoOriginal.setLugar(eventoAtualizado.getLugar());
            eventoOriginal.setDataHoraEvento(eventoAtualizado.getDataHoraEvento());
            eventoOriginal.setDescricao(eventoAtualizado.getDescricao());
            eventoRepository.save(eventoOriginal);
            return "redirect:/evento/" + id + "?from=meusEventos";
        }
        return "redirect:/";
    }

    // Exibe os eventos em que o usuário está inscrito, excluindo aqueles que ele mesmo criou.
    @GetMapping("/eventos-inscritos")
    public String exibirEventosInscritos(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        Usuario usuarioCompleto = usuarioRepository.findById(usuarioLogado.getId_usuario()).orElse(null);
        if (usuarioCompleto != null) {
            Set<Evento> eventosInscritos = usuarioCompleto.getEventosInscritos();
            List<Evento> eventosFiltrados = eventosInscritos.stream()
                    .filter(evento -> !evento.getUsuarioCriador().getId_usuario().equals(usuarioLogado.getId_usuario()))
                    .collect(Collectors.toList());
            model.addAttribute("eventos", eventosFiltrados);
        }
        model.addAttribute("activePage", "eventosInscritos");
        return "eventos-inscritos";
    }

    // Remove a inscrição do usuário do evento (se o evento não tiver passado).
    @PostMapping("/evento/{id}/cancelar-inscricao")
    public String cancelarInscricao(@PathVariable Long id, HttpSession session) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();

            // VERIFICAÇÃO DE DATA
            if (evento.getDataHoraEvento().isBefore(LocalDateTime.now())) {
                return "redirect:/evento/" + id;
            }

            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            evento.getParticipantes().remove(usuarioLogado);
            eventoRepository.save(evento);
        }
        return "redirect:/";
    }
}
