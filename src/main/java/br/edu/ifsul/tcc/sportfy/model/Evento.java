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
    private Long id_evento; // ID único do evento

    private String modalidadeEvento; // Modalidade do evento (ex: Futebol, Basquete)
    private String lugar; // Local do evento

    @Column(length = 355)
    private String descricao; // Descrição do evento, limitado a 355 caracteres

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataHoraEvento; // Data e hora do evento

    private LocalDateTime horario_de_postagem; // Data e hora em que o evento foi criado/postado

    @ManyToOne
    @JoinColumn(name = "id_usuario_criador")
    private Usuario usuarioCriador; // Usuário que criou o evento

    @ManyToMany
    @JoinTable(
        name = "evento_participante",
        joinColumns = @JoinColumn(name = "id_evento"),
        inverseJoinColumns = @JoinColumn(name = "id_usuario")
    )
    private Set<Usuario> participantes; // Usuários inscritos no evento

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comentario> comentarios; // Comentários relacionados a este evento
}
