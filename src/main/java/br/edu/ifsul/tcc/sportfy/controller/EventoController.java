package br.edu.ifsul.tcc.sportfy.controller;

import br.edu.ifsul.tcc.sportfy.model.Evento;
import br.edu.ifsul.tcc.sportfy.model.Usuario;
import br.edu.ifsul.tcc.sportfy.repository.EventoRepository;
import br.edu.ifsul.tcc.sportfy.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository; 

    @Autowired
    private UsuarioRepository usuarioRepository;


    // Home: lista eventos, com opção de filtrar por modalidade
    @GetMapping("/")
    public String exibirPaginaPrincipal(
            @RequestParam(name = "modalidade", required = false) String modalidade,
            Model model
    ) {
        List<Evento> eventos = (modalidade != null && !modalidade.isEmpty())
                ? eventoRepository.findByModalidadeEvento(modalidade)
                : eventoRepository.findAll();

        model.addAttribute("eventos", eventos);
        model.addAttribute("activePage", "eventos");
        model.addAttribute("selectedModalidade", modalidade);

        return "home";
    }

    // Formulário de novo evento
    @GetMapping("/eventos/novo")
    public String exibirFormularioNovoEvento(Model model) {
        model.addAttribute("evento", new Evento());
        model.addAttribute("activePage", "meusEventos");
        return "novo-evento";
    }

    // Salva novo evento
    @PostMapping("/eventos/novo")
    public String processarNovoEvento(
            @ModelAttribute Evento evento,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        evento.setUsuarioCriador(usuarioLogado);
        evento.setHorario_de_postagem(LocalDateTime.now());
        eventoRepository.save(evento);

        redirectAttributes.addFlashAttribute("toastMessage", "Evento criado com sucesso!");

        return "redirect:/";
    }

    // Confirma participação no evento
    @PostMapping("/evento/{id}/participar")
    public String participarDoEvento(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);

        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();

            if (evento.getDataHoraEvento().isBefore(LocalDateTime.now())) {
                return "redirect:/evento/" + id;
            }

            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
            evento.getParticipantes().add(usuarioLogado);
            eventoRepository.save(evento);

            redirectAttributes.addFlashAttribute("toastMessage", "Presença confirmada!");
        }

        return "redirect:/evento/" + id;
    }

    // Detalhes do evento
    @GetMapping("/evento/{id}")
    public String exibirDetalhesDoEvento(
            @PathVariable Long id,
            @RequestParam(name = "from", required = false, defaultValue = "eventos") String fromPage,
            Model model
    ) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);

        if (eventoOpt.isPresent()) {
            model.addAttribute("evento", eventoOpt.get());
            model.addAttribute("activePage", fromPage);
            return "evento-detalhes";
        }

        return "redirect:/";
    }

    // Lista eventos criados pelo usuário
    @GetMapping("/meus-eventos")
    public String exibirMeusEventos(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        List<Evento> eventos = eventoRepository.findByUsuarioCriador(usuarioLogado);

        model.addAttribute("eventos", eventos);
        model.addAttribute("activePage", "meusEventos");

        return "meus-eventos";
    }

    // Exclui evento criado pelo usuário
    @PostMapping("/eventos/excluir/{id}")
    public String excluirEvento(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);

        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

            if (evento.getDataHoraEvento().isBefore(LocalDateTime.now())) {
                return "redirect:/meus-eventos";
            }

            if (evento.getUsuarioCriador().getId_usuario().equals(usuarioLogado.getId_usuario())) {
                eventoRepository.deleteById(id);
                redirectAttributes.addFlashAttribute("toastMessage", "Evento excluído com sucesso.");
            }
        }

        return "redirect:/meus-eventos";
    }

    // Formulário de edição
    @GetMapping("/eventos/editar/{id}")
    public String exibirFormularioEdicao(
            @PathVariable Long id,
            Model model,
            HttpSession session
    ) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);

        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

            boolean naoPodeEditar =
                    evento.getDataHoraEvento().isBefore(LocalDateTime.now()) ||
                    !evento.getUsuarioCriador().getId_usuario().equals(usuarioLogado.getId_usuario());

            if (naoPodeEditar) return "redirect:/evento/" + id;

            model.addAttribute("evento", evento);
            model.addAttribute("activePage", "meusEventos");

            return "editar-evento";
        }

        return "redirect:/";
    }

    // Processa edição do evento
    @PostMapping("/eventos/editar/{id}")
    public String processarEdicaoEvento(
            @PathVariable Long id,
            @ModelAttribute Evento eventoAtualizado,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);

        if (eventoOpt.isPresent()) {
            Evento eventoOriginal = eventoOpt.get();
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

            boolean naoPodeEditar =
                    eventoOriginal.getDataHoraEvento().isBefore(LocalDateTime.now()) ||
                    !eventoOriginal.getUsuarioCriador().getId_usuario().equals(usuarioLogado.getId_usuario());

            if (naoPodeEditar) return "redirect:/evento/" + id;

            eventoOriginal.setModalidadeEvento(eventoAtualizado.getModalidadeEvento());
            eventoOriginal.setLugar(eventoAtualizado.getLugar());
            eventoOriginal.setDataHoraEvento(eventoAtualizado.getDataHoraEvento());
            eventoOriginal.setDescricao(eventoAtualizado.getDescricao());

            eventoRepository.save(eventoOriginal);

            redirectAttributes.addFlashAttribute("toastMessage", "Evento atualizado com sucesso!");

            return "redirect:/evento/" + id + "?from=meusEventos";
        }

        return "redirect:/";
    }

    // Lista eventos em que o usuário está inscrito (exceto os que criou)
    @GetMapping("/eventos-inscritos")
    public String exibirEventosInscritos(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        Usuario usuarioCompleto = usuarioRepository
                .findById(usuarioLogado.getId_usuario())
                .orElse(null);

        if (usuarioCompleto != null) {
            Set<Evento> eventosInscritos = usuarioCompleto.getEventosInscritos();

            List<Evento> eventosFiltrados = eventosInscritos.stream()
                    .filter(evento ->
                            !evento.getUsuarioCriador().getId_usuario().equals(usuarioLogado.getId_usuario()))
                    .collect(Collectors.toList());

            model.addAttribute("eventos", eventosFiltrados);
        }

        model.addAttribute("activePage", "eventosInscritos");

        return "eventos-inscritos";
    }

    // Cancela inscrição no evento
    @PostMapping("/evento/{id}/cancelar-inscricao")
    public String cancelarInscricao(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);

        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();

            if (evento.getDataHoraEvento().isBefore(LocalDateTime.now())) {
                return "redirect:/evento/" + id;
            }

            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

            evento.getParticipantes().remove(usuarioLogado);
            eventoRepository.save(evento);

            redirectAttributes.addFlashAttribute("toastMessage", "Inscrição cancelada.");
        }

        return "redirect:/evento/" + id;
    }
}
