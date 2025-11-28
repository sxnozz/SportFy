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
@EqualsAndHashCode(of = "id_usuario") 
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_usuario; 

    private String nome;

    @Column(unique = true)
    private String email; 

    private String senha; 

    @Column(length = 255)
    private String bio; 

    private String fotoPerfilUrl;

    private boolean perfilPrivado = false; 

    

    @OneToMany(mappedBy = "usuarioCriador")
    private Set<Evento> eventosCriados = new HashSet<>(); 

    @ManyToMany(mappedBy = "participantes")
    private Set<Evento> eventosInscritos = new HashSet<>(); 
}
