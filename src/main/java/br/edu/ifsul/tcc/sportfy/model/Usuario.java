// Em br.edu.ifsul.tcc.sportfy.model.Usuario.java

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
    
    // --- NOVOS CAMPOS ---
    
    @Column(length = 255) // Limita o tamanho da bio
    private String bio;
    
    private String fotoPerfilUrl; // Armazena o nome do arquivo da foto (ex: "meu-avatar.jpg")
    
    private boolean perfilPrivado = false; // Por padrão, o perfil é público
    
    // --- RELACIONAMENTOS (JÁ EXISTENTES) ---

    @OneToMany(mappedBy = "usuarioCriador")
    private Set<Evento> eventosCriados = new HashSet<>();

    @ManyToMany(mappedBy = "participantes")
    private Set<Evento> eventosInscritos = new HashSet<>();
    
}