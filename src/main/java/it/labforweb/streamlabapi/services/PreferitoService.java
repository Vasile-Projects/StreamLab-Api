package it.labforweb.streamlabapi.services;

import it.labforweb.streamlabapi.dtos.AggiungiPreferitoRequest;
import it.labforweb.streamlabapi.dtos.PreferitoResponse;
import it.labforweb.streamlabapi.exceptions.RisorsaGiaEsistenteException;
import it.labforweb.streamlabapi.exceptions.RisorsaNonTrovataException;
import it.labforweb.streamlabapi.models.Preferito;
import it.labforweb.streamlabapi.models.TipoTitolo;
import it.labforweb.streamlabapi.repositories.PreferitoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PreferitoService {
    private final PreferitoRepository preferitoRepository;

    public PreferitoService(PreferitoRepository preferitoRepository) {
        this.preferitoRepository = preferitoRepository;
    }

//  getAll

    public List<PreferitoResponse> getPreferiti(Integer userId, TipoTitolo tipo) {
        return preferitoRepository.findByUserIdAndTipo(userId, tipo).stream()
                .map(PreferitoResponse::new)
                .toList();
    }

//  aggiungi preferito
public PreferitoResponse aggiungi(Integer userId, AggiungiPreferitoRequest request) {

    if (preferitoRepository.existsByUserIdAndTmdbIdAndTipo(userId, request.tmdbId(), request.tipo())) {
        throw new RisorsaGiaEsistenteException("Il titolo è già presente nei preferiti");
    }

    Preferito preferito = new Preferito(null, userId, request.tmdbId(), request.tipo());
    preferitoRepository.save(preferito);

    return new PreferitoResponse(preferito);
}

    // rimuovi preferito

    public void rimuovi(Integer userId, Integer tmdbId, TipoTitolo tipo) {

        Preferito preferito = preferitoRepository.findByUserIdAndTmdbIdAndTipo(userId, tmdbId, tipo)
                .orElseThrow(() -> new RisorsaNonTrovataException("Titolo non trovato tra i tuoi preferiti"));

        preferitoRepository.delete(preferito);
    }


}
