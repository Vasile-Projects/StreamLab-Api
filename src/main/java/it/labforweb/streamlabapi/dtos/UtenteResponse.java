package it.labforweb.streamlabapi.dtos;

import it.labforweb.streamlabapi.models.AbbonamentoUtente;
import it.labforweb.streamlabapi.models.Utente;

public record UtenteResponse(
        Integer idUtente,
        String nome,
        String cognome,
        String email,
        Integer idAbbonamento
) {

    public UtenteResponse(Utente utente, AbbonamentoUtente abbonamentoUtente) {
        this(
                utente.getId(),
                utente.getNome(),
                utente.getCognome(),
                utente.getEmail(),
                abbonamentoUtente.getTipoAbbonamentoId()
        );
    }
}
