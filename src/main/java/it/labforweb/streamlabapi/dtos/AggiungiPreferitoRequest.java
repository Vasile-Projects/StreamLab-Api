package it.labforweb.streamlabapi.dtos;

import it.labforweb.streamlabapi.models.TipoTitolo;
import jakarta.validation.constraints.NotNull;

public record AggiungiPreferitoRequest(
        @NotNull(message = "L'id del titolo è obbligatorio")
        Integer tmdbId,

        @NotNull(message = "Il tipo è obbligatorio")
        TipoTitolo tipo
) {}