package br.edu.ifsul.tcc.sportfy.controller;

import br.edu.ifsul.tcc.sportfy.model.Evento;
import br.edu.ifsul.tcc.sportfy.model.Usuario;
import br.edu.ifsul.tcc.sportfy.repository.EventoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable; // NOVO IMPORT
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // NOVO IMPORT

@Controller
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    // --- PÁGINA PRINCIPAL ---
    @GetMapping("/")
    public String exibirPaginaPrincipal(Model model) {
        List<Evento> eventos = eventoRepository.findAll();
        model.addAttribute("eventos", eventos);
        model.addAttribute("activePage", "eventos"); 
        return "home";
    }

    // --- MÉTODOS PARA CRIAÇÃO DE EVENTO ---

    // 1. Método para MOSTRAR o formulário de criação
    @GetMapping("/eventos/novo")
    public String exibirFormularioNovoEvento(Model model) {
        model.addAttribute("evento", new Evento());
        model.addAttribute("activePage", "meusEventos"); // Mantém um menu ativo
        return "novo-evento";
    }

    // 2. Método para PROCESSAR os dados do formulário enviado
    @PostMapping("/eventos/novo")
    public String processarNovoEvento(@ModelAttribute Evento evento, HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        evento.setUsuario_criador(usuarioLogado);
        evento.setHorario_de_postagem(LocalDateTime.now());
        eventoRepository.save(evento);

        return "redirect:/";
    }





 @PostMapping("/evento/{id}/participar")
    public String participarDoEvento(@PathVariable Long id, HttpSession session) {
        // Pega o utilizador que está autenticado na sessão
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Medida de segurança: se não houver utilizador autenticado, redireciona para o login
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Procura o evento na base de dados
        Optional<Evento> eventoOpt = eventoRepository.findById(id);
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();
            
            // Adiciona o utilizador autenticado à lista de participantes do evento
            // O Spring/JPA gere a tabela de ligação 'evento_participante' por nós
            evento.getParticipantes().add(usuarioLogado);
            
            // Guarda o evento atualizado na base de dados
            eventoRepository.save(evento);
        }

        // Redireciona de volta para a mesma página de detalhes do evento
        return "redirect:/evento/" + id;
    }

    
    // --- NOVO MÉTODO PARA EXIBIR DETALHES DO EVENTO ---
    @GetMapping("/evento/{id}")
    public String exibirDetalhesDoEvento(@PathVariable Long id, Model model) {
        Optional<Evento> eventoOpt = eventoRepository.findById(id);

        if (eventoOpt.isPresent()) {
            model.addAttribute("evento", eventoOpt.get());
            model.addAttribute("activePage", "eventos"); // Mantém o menu "Eventos" ativo
            return "evento-detalhes"; // Renderiza a nova página de detalhes
        } else {
            return "redirect:/"; // Se não encontrar o evento, volta para a home
        }
    }
}