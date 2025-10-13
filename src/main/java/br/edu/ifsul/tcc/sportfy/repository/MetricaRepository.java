package br.edu.ifsul.tcc.sportfy.repository;

import br.edu.ifsul.tcc.sportfy.model.Metrica;
import br.edu.ifsul.tcc.sportfy.model.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface MetricaRepository extends JpaRepository<Metrica, Long> {



     // NOVO MÉTODO: Encontra todas as métricas de um usuário, ordenadas pela data mais recente
    List<Metrica> findByUsuarioOrderByDiaMetricaDesc(Usuario usuario);
}