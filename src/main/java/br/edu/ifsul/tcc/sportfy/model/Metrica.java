package br.edu.ifsul.tcc.sportfy.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
@Setter
public class Metrica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_metrica; // ID único da métrica

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaMetrica; // Data da métrica

    private String esporte; // Esporte relacionado à métrica (Futebol, Basquete, Vôlei)

    // Métricas específicas de cada esporte
    private Integer gols;
    private Integer assistencias_futebol;
    private Integer desarmes;
    private Integer pontos_basquete;
    private Integer assistencias_basquete;
    private Integer rebotes;
    private Integer pontos_volei;
    private Integer aces;
    private Integer bloqueios;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario; // Usuário dono da métrica
}
