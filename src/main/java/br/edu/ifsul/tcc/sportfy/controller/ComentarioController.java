package br.edu.ifsul.tcc.sportfy.controller;

import br.edu.ifsul.tcc.sportfy.model.Comentario;
import br.edu.ifsul.tcc.sportfy.model.Evento;
import br.edu.ifsul.tcc.sportfy.model.Usuario;
import br.edu.ifsul.tcc.sportfy.repository.ComentarioRepository;
import br.edu.ifsul.tcc.sportfy.repository.EventoRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class ComentarioController {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private EventoRepository eventoRepository;

    // Salva um comentário no evento
    @PostMapping("/evento/{eventoId}/comentar")
    public String adicionarComentario(@PathVariable Long eventoId,
                                      @RequestParam("texto_comentario") String textoComentario,
                                      HttpSession session) {

        // Verifica usuário logado
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Optional<Evento> eventoOpt = eventoRepository.findById(eventoId);
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();

            // Impede comentário após o evento ter passado
            if (evento.getDataHoraEvento().isBefore(LocalDateTime.now())) {
                return "redirect:/evento/" + eventoId;
            }

            // Cria e salva o comentário
            Comentario novoComentario = new Comentario();
            novoComentario.setTexto_comentario(textoComentario);
            novoComentario.setHorario_comentario(LocalDateTime.now());
            novoComentario.setUsuario(usuarioLogado);
            novoComentario.setEvento(evento);

            comentarioRepository.save(novoComentario);
        }

        return "redirect:/evento/" + eventoId;
    }
}
