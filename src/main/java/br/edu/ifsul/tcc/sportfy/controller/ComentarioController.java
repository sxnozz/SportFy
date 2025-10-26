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

    // Repositório para persistir objetos Comentario.
    @Autowired
    private ComentarioRepository comentarioRepository;

    // Repositório para recuperar informações do Evento.
    @Autowired
    private EventoRepository eventoRepository;

    // Recebe POST em /evento/{eventoId}/comentar, cria e salva um comentário
    // associado ao usuário logado e ao evento, se o evento ainda não tiver passado.
    // Parâmetros:
    //  - eventoId: id do evento (PathVariable)
    //  - texto_comentario: texto do comentário (RequestParam)
    //  - session: sessão HTTP (para obter usuário logado)
    @PostMapping("/evento/{eventoId}/comentar")
    public String adicionarComentario(@PathVariable Long eventoId,
                                      @RequestParam("texto_comentario") String textoComentario,
                                      HttpSession session) {

        // A verificação do Interceptor já garante que o usuário está logado,
        // mas mantemos por segurança caso o interceptor seja desativado.
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        Optional<Evento> eventoOpt = eventoRepository.findById(eventoId);
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();

            // --- VERIFICAÇÃO DE DATA ---
            // Se a data do evento for anterior a agora, o evento já passou.
            if (evento.getDataHoraEvento().isBefore(LocalDateTime.now())) {
                // Apenas redireciona de volta para a página, sem salvar o comentário.
                return "redirect:/evento/" + eventoId;
            }
            // --- FIM DA VERIFICAÇÃO ---

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
