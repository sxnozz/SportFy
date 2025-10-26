package br.edu.ifsul.tcc.sportfy.repository;

import br.edu.ifsul.tcc.sportfy.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca um usuário pelo email (útil para login)
    Usuario findByEmail(String email);

    // Busca um usuário pelo nome (útil para validações de cadastro)
    Usuario findByNome(String nome);
}
