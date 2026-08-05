package it.labforweb.streamlabapi.services;

import it.labforweb.streamlabapi.dtos.AbbonamentoResponse;
import it.labforweb.streamlabapi.repositories.AbbonamentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AbbonamentoService {

    private final AbbonamentoRepository abbonamentoRepository;

    public AbbonamentoService(AbbonamentoRepository abbonamentoRepository) {
        this.abbonamentoRepository = abbonamentoRepository;
    }

    public List<AbbonamentoResponse> getAbbonamenti() {
        return abbonamentoRepository.findAll().stream()
                .map(AbbonamentoResponse::new)
                .toList();
    }
}
