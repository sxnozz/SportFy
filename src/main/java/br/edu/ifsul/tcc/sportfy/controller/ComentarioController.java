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

    @PostMapping("/evento/{eventoId}/comentar")
    public String adicionarComentario(@PathVariable Long eventoId, 
                                      @RequestParam("texto_comentario") String textoComentario, 
                                      HttpSession session) {

        // Pega o utilizador que está autenticado na sessão
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Medida de segurança: se não houver utilizador autenticado, redireciona para o login
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        // Procura o evento na base de dados
        Optional<Evento> eventoOpt = eventoRepository.findById(eventoId);
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();

            // Cria um novo objeto Comentario
            Comentario novoComentario = new Comentario();
            novoComentario.setTexto_comentario(textoComentario);
            novoComentario.setHorario_comentario(LocalDateTime.now());
            novoComentario.setUsuario(usuarioLogado); // Associa o utilizador ao comentário
            novoComentario.setEvento(evento);       // Associa o evento ao comentário

            // Guarda o novo comentário na base de dados
            comentarioRepository.save(novoComentario);
        }

        // Redireciona de volta para a mesma página de detalhes do evento
        return "redirect:/evento/" + eventoId;
    }
}