package it.labforweb.streamlabapi.dtos;

import it.labforweb.streamlabapi.models.Preferito;
import it.labforweb.streamlabapi.models.TipoTitolo;

public record PreferitoResponse(Integer tmdbId, TipoTitolo tipo) {
    public PreferitoResponse(Preferito preferito) {
        this(preferito.getTmdbId(), preferito.getTipo());
    }
}