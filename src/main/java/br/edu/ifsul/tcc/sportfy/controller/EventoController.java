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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository; // acesso ao banco de eventos

    @Autowired
    private UsuarioRepository usuarioRepository; // acesso ao banco de usuários


    /**
     * GET /
     * Página inicial (home): lista os eventos.
     * Se "modalidade" vier na URL (?modalidade=Futebol), filtra só daquela modalidade.
     * Também marca qual aba fica ativa no header.
     */
    @GetMapping("/")
    public String exibirPaginaPrincipal(
            @RequestParam(name = "modalidade", required = false) String modalidade,
            Model model
    ) {
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


    /**
     * GET /eventos/novo
     * Mostra o formulário para criar um novo evento.
     * Coloca um objeto Evento vazio no model para preencher via th:field.
     */
    @GetMapping("/eventos/novo")
    public String exibirFormularioNovoEvento(Model model) {
        model.addAttribute("evento", new Evento());
        model.addAttribute("activePage", "meusEventos");
        return "novo-evento";
    }


    /**
     * POST /eventos/novo
     * Salva um novo evento criado pelo usuário logado.
     * Define criador e horário de postagem antes de salvar.
     * Depois redireciona para a home e exibe um toast de sucesso.
     */
    @PostMapping("/eventos/novo")
    public String processarNovoEvento(
            @ModelAttribute Evento evento,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        evento.setUsuarioCriador(usuarioLogado);
        evento.setHorario_de_postagem(LocalDateTime.now());
        eventoRepository.save(evento);

        // mensagem rápida pro usuário (toast)
        redirectAttributes.addFlashAttribute("toastMessage", "Evento criado com sucesso!");

        return "redirect:/";
    }


    /**
     * POST /evento/{id}/participar
     * Marca o usuário logado como participante do evento informado.
     * Só permite se o evento ainda não aconteceu.
     * Depois volta pros detalhes do evento e mostra um toast.
     */
    @PostMapping("/evento/{id}/participar")
    public String participarDoEvento(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);

        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();

            // impede confirmação em evento passado
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


    /**
     * GET /evento/{id}
     * Mostra a página de detalhes do evento:
     * - informações básicas
     * - lista de participantes
     * - comentários
     *
     * O parâmetro "from" serve pra manter o destaque correto no menu (ex: "meusEventos").
     */
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
        } else {
            return "redirect:/";
        }
    }


    /**
     * GET /meus-eventos
     * Lista os eventos criados pelo usuário logado.
     */
    @GetMapping("/meus-eventos")
    public String exibirMeusEventos(Model model, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        List<Evento> eventos = eventoRepository.findByUsuarioCriador(usuarioLogado);

        model.addAttribute("eventos", eventos);
        model.addAttribute("activePage", "meusEventos");

        return "meus-eventos";
    }


    /**
     * POST /eventos/excluir/{id}
     * Exclui um evento SE:
     * - o evento ainda não passou
     * - o usuário logado é o criador
     *
     * Depois redireciona pra "Meus Eventos" com toast.
     */
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

            // se já passou, não deixa excluir
            if (evento.getDataHoraEvento().isBefore(LocalDateTime.now())) {
                return "redirect:/meus-eventos";
            }

            // só o dono pode deletar
            if (evento.getUsuarioCriador().getId_usuario().equals(usuarioLogado.getId_usuario())) {
                eventoRepository.deleteById(id);

                redirectAttributes.addFlashAttribute("toastMessage", "Evento excluído com sucesso.");
            }
        }

        return "redirect:/meus-eventos";
    }


    /**
     * GET /eventos/editar/{id}
     * Mostra o formulário de edição do evento.
     * Só permite editar se:
     * - o evento ainda vai acontecer
     * - o usuário logado é o criador
     */
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

            if (naoPodeEditar) {
                return "redirect:/evento/" + id;
            }

            model.addAttribute("evento", evento);
            model.addAttribute("activePage", "meusEventos");

            return "editar-evento";
        }

        return "redirect:/";
    }


    /**
     * POST /eventos/editar/{id}
     * Recebe os dados editados do formulário e atualiza o evento original.
     * Depois redireciona pros detalhes do evento com um toast.
     */
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

            if (naoPodeEditar) {
                return "redirect:/evento/" + id;
            }

            // atualiza só os campos que podem ser editados
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


    /**
     * GET /eventos-inscritos
     * Lista os eventos em que o usuário está como participante,
     * mas remove da listagem os eventos que ele mesmo criou (pra não duplicar com "Meus Eventos").
     */
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


    /**
     * POST /evento/{id}/cancelar-inscricao
     * Remove o usuário logado da lista de participantes do evento.
     * Só funciona se o evento ainda vai acontecer.
     * Depois redireciona pra home com toast.
     */
    @PostMapping("/evento/{id}/cancelar-inscricao")
    public String cancelarInscricao(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);

        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();

            // se já passou, não faz nada
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
