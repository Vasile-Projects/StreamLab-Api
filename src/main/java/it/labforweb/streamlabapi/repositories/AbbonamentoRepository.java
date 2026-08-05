package it.labforweb.streamlabapi.repositories;

import it.labforweb.streamlabapi.models.Abbonamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbbonamentoRepository extends JpaRepository<Abbonamento, Integer> {

}
