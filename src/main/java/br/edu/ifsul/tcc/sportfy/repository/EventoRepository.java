package br.edu.ifsul.tcc.sportfy.repository;

import br.edu.ifsul.tcc.sportfy.model.Evento;
import br.edu.ifsul.tcc.sportfy.model.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    // Retorna todos os eventos criados por um determinado usuário
    List<Evento> findByUsuarioCriador(Usuario usuarioCriador);

    // Retorna todos os eventos filtrados por modalidade específica
    List<Evento> findByModalidadeEvento(String modalidade);
}
