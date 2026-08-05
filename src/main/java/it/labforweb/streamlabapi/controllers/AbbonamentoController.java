package it.labforweb.streamlabapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.labforweb.streamlabapi.dtos.AbbonamentoResponse;
import it.labforweb.streamlabapi.services.AbbonamentoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Abbonamenti", description = "Catalogo pubblico dei piani disponibili — nessuna autenticazione richiesta")
@RestController
@RequestMapping("/api/v1/abbonamenti")
public class AbbonamentoController {

    private final AbbonamentoService abbonamentoService;

    public AbbonamentoController(AbbonamentoService abbonamentoService) {
        this.abbonamentoService = abbonamentoService;
    }

    @Operation(
            summary = "Elenca i piani di abbonamento disponibili",
            description = "Endpoint pubblico, usato sia nella pagina di registrazione sia per mostrare le opzioni di cambio piano."
    )
    @ApiResponse(responseCode = "200", description = "Elenco piani restituito")
    @GetMapping
    public ResponseEntity<List<AbbonamentoResponse>> getAbbonamenti() {
        return ResponseEntity.ok(abbonamentoService.getAbbonamenti());
    }
}