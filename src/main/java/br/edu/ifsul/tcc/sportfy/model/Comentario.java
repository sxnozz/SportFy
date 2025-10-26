package br.edu.ifsul.tcc.sportfy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_comentario; // ID único do comentário

    private String texto_comentario; // Texto do comentário
    private LocalDateTime horario_comentario; // Data e hora em que o comentário foi feito

    @ManyToOne
    @JoinColumn(name = "id_evento")
    private Evento evento; // Evento ao qual o comentário pertence

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario; // Usuário que fez o comentário

    // Lombok gera automaticamente os getters e setters
}
