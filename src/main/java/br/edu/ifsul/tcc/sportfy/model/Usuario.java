package br.edu.ifsul.tcc.sportfy.model;

import jakarta.persistence.*;
import java.util.Set;

// Adicione os imports do Lombok aqui
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter // <-- Adicione esta anotação
@Setter // <-- Adicione esta anotação
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_usuario;
    
    private String nome;
    
    @Column(unique = true)
    private String email;
    
    private String senha;
    
    @Lob
    private byte[] foto_usuario;

    @OneToMany(mappedBy = "usuario_criador")
    private Set<Evento> eventosCriados;

    @ManyToMany(mappedBy = "participantes")
    private Set<Evento> eventosInscritos;
    
    // Agora você pode apagar todos os getters e setters manuais que tínhamos criado antes!
}