package it.labforweb.streamlabapi.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.labforweb.streamlabapi.dtos.LoginRequest;
import it.labforweb.streamlabapi.dtos.RegistrazioneRequest;
import it.labforweb.streamlabapi.exceptions.Errore;
import it.labforweb.streamlabapi.exceptions.NonAutorizzatoException;
import it.labforweb.streamlabapi.security.JwtProperties;
import it.labforweb.streamlabapi.services.AuthService;
import it.labforweb.streamlabapi.utils.TokenPair;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;


@Tag(name = "Autenticazione", description = "Login, registrazione, refresh e logout")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    public AuthController(AuthService authService, JwtProperties jwtProperties) {
        this.authService = authService;
        this.jwtProperties = jwtProperties;
    }

    @Operation(
            summary = "Registra un nuovo utente",
            description = "Crea un nuovo account con abbonamento associato nella stessa operazione. " +
                    "Se l'email corrisponde a un account esistente ma disattivato, l'account viene riattivato e aggiornato con i dati forniti."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utente registrato (o riattivato) con successo"),
            @ApiResponse(responseCode = "400", description = "Dati non validi o piano abbonamento inesistente",
                    content = @Content(schema = @Schema(implementation = Errore.class))),
            @ApiResponse(responseCode = "409", description = "Email già registrata su un account attivo",
                    content = @Content(schema = @Schema(implementation = Errore.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegistrazioneRequest req) {
        authService.registraNuovoUtente(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Effettua il login",
            description = "Verifica le credenziali e imposta due cookie httpOnly: 'accessToken' (durata breve, per l'autenticazione su ogni richiesta) " +
                    "e 'refreshToken' (durata lunga, path ristretto a /api/v1/auth, usato solo per rinnovare l'access token e per il logout). " +
                    "Nessun dato utente nel body: chiamare GET /api/v1/me subito dopo per recuperare il profilo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login riuscito, cookie impostati nella risposta"),
            @ApiResponse(responseCode = "401", description = "Email non trovata o password errata",
                    content = @Content(schema = @Schema(implementation = Errore.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest req) {
        TokenPair tokens = authService.autentica(req);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", tokens.accessJwt())
                .httpOnly(true).secure(true).path("/").sameSite("Strict")
                .maxAge(Duration.ofMillis(jwtProperties.getAccessTokenExpirationMs()))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokens.refreshJwt())
                .httpOnly(true).secure(true).path("/api/v1/auth").sameSite("Strict")
                .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @Operation(
            summary = "Rinnova l'access token",
            description = "Usa il refresh token (letto dal cookie, path /api/v1/auth) per verificarne validità a database " +
                    "(esistenza, non revocato, non scaduto) ed emettere un nuovo access token. Non richiede un access token valido."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nuovo access token emesso nel cookie"),
            @ApiResponse(responseCode = "401", description = "Refresh token mancante, non trovato, revocato o scaduto",
                    content = @Content(schema = @Schema(implementation = Errore.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @Parameter(description = "Refresh token letto dal cookie httpOnly", required = false)
            @CookieValue(value = "refreshToken", required = false) String refreshJwt
    ) {
        if (refreshJwt == null) {
            throw new NonAutorizzatoException("Sessione scaduta, effettua nuovamente il login");
        }

        String nuovoAccessJwt = authService.refresh(refreshJwt);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", nuovoAccessJwt)
                .httpOnly(true).secure(true).path("/").sameSite("Strict")
                .maxAge(Duration.ofMillis(jwtProperties.getAccessTokenExpirationMs()))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .build();
    }

    @Operation(
            summary = "Effettua il logout",
            description = "Revoca a database il refresh token della sessione corrente (le altre sessioni/dispositivi restano attivi) " +
                    "e scade entrambi i cookie. Non richiede un access token valido."
    )
    @ApiResponse(responseCode = "200", description = "Logout effettuato, cookie scaduti")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Refresh token letto dal cookie httpOnly", required = false)
            @CookieValue(value = "refreshToken", required = false) String refreshJwt
    ) {
        if (refreshJwt != null) {
            authService.logout(refreshJwt);
        }

        ResponseCookie accessCookieScaduto = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(true).path("/").sameSite("Strict")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookieScaduto = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).path("/api/v1/auth").sameSite("Strict")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookieScaduto.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookieScaduto.toString())
                .build();
    }
}