package it.labforweb.streamlabapi.dtos;

import it.labforweb.streamlabapi.models.Abbonamento;

import java.math.BigDecimal;
import java.util.List;

public record AbbonamentoResponse(Integer id, String nome, String risoluzione, List<String> vantaggi, BigDecimal prezzoMensile) {

    public AbbonamentoResponse(Abbonamento abbonamento) {
        this(
                abbonamento.getId(),
                abbonamento.getNome(),
                abbonamento.getQualita(),
                abbonamento.getFeatures(),
                abbonamento.getPrezzo()
        );
    }
}