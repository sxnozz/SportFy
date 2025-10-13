package br.edu.ifsul.tcc.sportfy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter // <-- Adicione esta anotação
@Setter // <-- Adicione esta anotação
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_evento;
    private String modalidade_evento;
    private String lugar;
    @Column(length = 355)
    private String descricao;
    private LocalDateTime data_hora_evento;
    private LocalDateTime horario_de_postagem;

    @ManyToOne
    @JoinColumn(name = "id_usuario_criador")
    private Usuario usuario_criador;

    @ManyToMany
    @JoinTable(
        name = "evento_participante",
        joinColumns = @JoinColumn(name = "id_evento"),
        inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    private Set<Usuario> participantes;

    @OneToMany(mappedBy = "evento")
    private Set<Comentario> comentarios;

}