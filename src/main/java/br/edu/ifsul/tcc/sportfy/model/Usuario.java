package br.edu.ifsul.tcc.sportfy.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id_usuario") // Garante comparação por ID único
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_usuario; // ID único do usuário

    private String nome; // Nome do usuário

    @Column(unique = true)
    private String email; // Email único, usado para login

    private String senha; // Senha do usuário (atenção: atualmente sem criptografia)

    @Column(length = 255)
    private String bio; // Biografia do usuário, limite de 255 caracteres

    private String fotoPerfilUrl; // Armazena nome do arquivo da foto do perfil

    private boolean perfilPrivado = false; // Define se o perfil é privado ou público

    // --- RELACIONAMENTOS ---

    @OneToMany(mappedBy = "usuarioCriador")
    private Set<Evento> eventosCriados = new HashSet<>(); // Eventos que o usuário criou

    @ManyToMany(mappedBy = "participantes")
    private Set<Evento> eventosInscritos = new HashSet<>(); // Eventos em que o usuário está inscrito
}
