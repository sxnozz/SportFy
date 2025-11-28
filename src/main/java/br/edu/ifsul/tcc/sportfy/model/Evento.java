package br.edu.ifsul.tcc.sportfy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
@Setter
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_evento; 

    private String modalidadeEvento; 
    private String lugar;

    @Column(length = 355)
    private String descricao; 

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataHoraEvento; 

    private LocalDateTime horario_de_postagem;

    @ManyToOne
    @JoinColumn(name = "id_usuario_criador")
    private Usuario usuarioCriador; 

    @ManyToMany
    @JoinTable(
        name = "evento_participante",
        joinColumns = @JoinColumn(name = "id_evento"),
        inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    private Set<Usuario> participantes; 

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comentario> comentarios;
}
