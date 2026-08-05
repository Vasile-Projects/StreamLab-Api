package it.labforweb.streamlabapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.labforweb.streamlabapi.dtos.CambioPasswordRequest;
import it.labforweb.streamlabapi.dtos.MessaggioResponse;
import it.labforweb.streamlabapi.dtos.UtenteResponse;
import it.labforweb.streamlabapi.exceptions.Errore;
import it.labforweb.streamlabapi.services.UtenteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Profilo utente", description = "Lettura e modifica del profilo dell'utente autenticato. Richiede un cookie 'accessToken' valido.")
@SecurityRequirement(name = "cookieAuth")
@Validated
@RestController
@RequestMapping("/api/v1/me")
public class UtenteController {

    private final UtenteService utenteService;

    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @Operation(
            summary = "Recupera il profilo dell'utente autenticato",
            description = "Restituisce dati anagrafici ed id del piano di abbonamento corrente. " +
                    "Da chiamare al bootstrap dell'app e subito dopo un login riuscito."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profilo restituito"),
            @ApiResponse(responseCode = "401", description = "Access token mancante, scaduto o non valido",
                    content = @Content(schema = @Schema(implementation = Errore.class))),
            @ApiResponse(responseCode = "404", description = "Utente o abbonamento non trovato",
                    content = @Content(schema = @Schema(implementation = Errore.class)))
    })
    @GetMapping
    public ResponseEntity<UtenteResponse> me(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        return ResponseEntity.ok(utenteService.me(userId));
    }

    @Operation(
            summary = "Cambia la password",
            description = "Richiede la password attuale per verificarne la corrispondenza prima di impostare quella nuova."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password aggiornata con successo"),
            @ApiResponse(responseCode = "400", description = "Nuova password non valida (formato/lunghezza)",
                    content = @Content(schema = @Schema(implementation = Errore.class))),
            @ApiResponse(responseCode = "401", description = "Access token non valido, o password attuale errata",
                    content = @Content(schema = @Schema(implementation = Errore.class)))
    })
    @PutMapping("/password")
    public ResponseEntity<MessaggioResponse> aggiornaPassword(HttpServletRequest request, @Valid @RequestBody CambioPasswordRequest body) {
        Integer userId = (Integer) request.getAttribute("userId");
        utenteService.aggiornaPassword(userId, body);
        return ResponseEntity.ok(new MessaggioResponse("Password aggiornata con successo"));
    }

    @Operation(
            summary = "Cambia il piano di abbonamento",
            description = "Verifica che il piano richiesto esista nel catalogo, aggiorna l'associazione utente-piano " +
                    "e restituisce subito il profilo aggiornato (nessuna chiamata separata a /me necessaria)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Piano aggiornato, profilo restituito"),
            @ApiResponse(responseCode = "400", description = "Piano abbonamento inesistente",
                    content = @Content(schema = @Schema(implementation = Errore.class))),
            @ApiResponse(responseCode = "401", description = "Access token non valido",
                    content = @Content(schema = @Schema(implementation = Errore.class)))
    })
    @PutMapping("/abbonamento/{tipoAbbonamentoId}")
    public ResponseEntity<UtenteResponse> cambiaAbbonamento(HttpServletRequest request,
                                                            @Positive(message = "Id abbonamento non valido")
                                                            @PathVariable Integer tipoAbbonamentoId) {
        Integer userId = (Integer) request.getAttribute("userId");
        UtenteResponse utente = utenteService.cambiaAbbonamento(userId, tipoAbbonamentoId);
        return ResponseEntity.ok(utente);
    }

    @DeleteMapping
    public ResponseEntity<Void> disattivaAccount(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");

        utenteService.disattivaAccount(userId);

        ResponseCookie accessCookieScaduto = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(true).path("/").sameSite("Strict")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookieScaduto = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).path("/api/v1/auth/refresh").sameSite("Strict")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookieScaduto.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookieScaduto.toString())
                .build();
    }
}