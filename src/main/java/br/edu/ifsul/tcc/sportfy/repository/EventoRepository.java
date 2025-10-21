package br.edu.ifsul.tcc.sportfy.repository;

import br.edu.ifsul.tcc.sportfy.model.Evento;
import br.edu.ifsul.tcc.sportfy.model.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
    // Por enquanto, os métodos padrão do JpaRepository são suficientes

    List<Evento> findByUsuarioCriador(Usuario usuarioCriador);


List<Evento> findByModalidadeEvento(String modalidade);}

