package br.edu.ifsul.tcc.sportfy.repository;

import br.edu.ifsul.tcc.sportfy.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
}