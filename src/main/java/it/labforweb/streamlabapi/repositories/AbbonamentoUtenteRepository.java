package it.labforweb.streamlabapi.repositories;

import it.labforweb.streamlabapi.models.AbbonamentoUtente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AbbonamentoUtenteRepository extends JpaRepository<AbbonamentoUtente, Integer> {

    Optional<AbbonamentoUtente> findByUserId(Integer userId);
}
