package it.labforweb.streamlabapi.controllers;

import it.labforweb.streamlabapi.dtos.AggiungiPreferitoRequest;
import it.labforweb.streamlabapi.dtos.PreferitoResponse;
import it.labforweb.streamlabapi.models.TipoTitolo;
import it.labforweb.streamlabapi.services.PreferitoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/preferiti")
public class PreferitoController {

    private final PreferitoService preferitoService;

    public PreferitoController(PreferitoService preferitoService) {
        this.preferitoService = preferitoService;
    }

    @GetMapping
    public ResponseEntity<List<PreferitoResponse>> getPreferiti(HttpServletRequest request, @RequestParam TipoTitolo tipo) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ResponseEntity.ok(preferitoService.getPreferiti(userId, tipo));
    }

    @PostMapping
    public ResponseEntity<PreferitoResponse> aggiungiPreferito(HttpServletRequest request, @Valid @RequestBody AggiungiPreferitoRequest body) {
        Integer userId = (Integer) request.getAttribute("userId");
        PreferitoResponse response = preferitoService.aggiungi(userId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{tipo}/{tmdbId}")
    public ResponseEntity<Void> rimuoviPreferito(HttpServletRequest request, @PathVariable TipoTitolo tipo, @PathVariable Integer tmdbId) {
        Integer userId = (Integer) request.getAttribute("userId");
        preferitoService.rimuovi(userId, tmdbId, tipo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}