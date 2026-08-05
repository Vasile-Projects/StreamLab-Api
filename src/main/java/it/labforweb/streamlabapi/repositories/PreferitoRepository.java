package it.labforweb.streamlabapi.repositories;

import it.labforweb.streamlabapi.models.Preferito;
import it.labforweb.streamlabapi.models.TipoTitolo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PreferitoRepository extends JpaRepository<Preferito, Integer> {

    public List<Preferito> findByUserIdAndTipo(Integer userId, TipoTitolo tipo);

    Optional<Preferito> findByUserIdAndTmdbIdAndTipo(Integer userId, Integer tmdbId, TipoTitolo tipo);

    public boolean existsByUserIdAndTmdbIdAndTipo(Integer userId, Integer tmdbId, TipoTitolo tipo);
}
