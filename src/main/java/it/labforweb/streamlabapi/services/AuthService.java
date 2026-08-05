package it.labforweb.streamlabapi.services;

import it.labforweb.streamlabapi.dtos.LoginRequest;
import it.labforweb.streamlabapi.dtos.RegistrazioneRequest;
import it.labforweb.streamlabapi.exceptions.*;
import it.labforweb.streamlabapi.models.AbbonamentoUtente;
import it.labforweb.streamlabapi.models.RefreshToken;
import it.labforweb.streamlabapi.models.Utente;
import it.labforweb.streamlabapi.repositories.AbbonamentoRepository;
import it.labforweb.streamlabapi.repositories.AbbonamentoUtenteRepository;
import it.labforweb.streamlabapi.repositories.RefreshTokenRepository;
import it.labforweb.streamlabapi.repositories.UtenteRepository;
import it.labforweb.streamlabapi.security.JwtProperties;
import it.labforweb.streamlabapi.security.JwtService;
import it.labforweb.streamlabapi.utils.TokenPair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final JwtService jwtService;
    private final UtenteRepository utenteRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final AbbonamentoRepository abbonamentoRepository;
    private final AbbonamentoUtenteRepository abbonamentoUtenteRepository;

    public AuthService(JwtService jwtService, UtenteRepository utenteRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtProperties jwtProperties,
                       AbbonamentoUtenteRepository abbonamentoUtenteRepository,
                       AbbonamentoRepository abbonamentoRepository) {
        this.jwtService = jwtService;
        this.utenteRepository = utenteRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProperties = jwtProperties;
        this.abbonamentoUtenteRepository = abbonamentoUtenteRepository;
        this.abbonamentoRepository = abbonamentoRepository;
    }

    public TokenPair autentica(LoginRequest req) {

        //verifico se l'email presa dalla login request esiste nel database : se esiste mi torna l'utente
        Utente utente = utenteRepository.findByEmail(req.email())
                .orElseThrow(() -> new NonAutorizzatoException("Credenziali non valide"));

        //verifico se la password della request matcha con la password del db
        if (!passwordEncoder.matches(req.password(), utente.getHashedPassword())) {
            throw new NonAutorizzatoException("Credenziali non valide");
        }

        //verifico se l'utente è attivo (non è un account disattivato)

        if (!utente.isAttivo()) {
            throw new NonAutorizzatoException("Credenziali non valide");
        }

        //se sono qui le password matchano, posso generare l'access e il refresh token
        String accessJwt = jwtService.generateAccessToken(utente);
        String refreshJwt = jwtService.generateRefreshToken(utente);

        // costruisco e salvo l'entity del refresh token
        RefreshToken refreshTokenEntity = new RefreshToken(
                null,
                utente.getId(),
                refreshJwt,
                LocalDateTime.now(),
                LocalDateTime.now().plus(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs())),
                false
        );
        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenPair(accessJwt, refreshJwt);
    }

    public String refresh(String refreshJwt) {

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshJwt)
                .orElseThrow(() -> new NonAutorizzatoException("Sessione scaduta, effettua nuovamente il login"));

        //refreshToken trovato, verifico validità e scadenza
        if (refreshTokenEntity.isRevocato() || refreshTokenEntity.getDataScadenza().isBefore(LocalDateTime.now())) {
            throw new NonAutorizzatoException("Sessione scaduta, effettua nuovamente il login");
        }

        //se sono qui il refreshToken esiste, non è revocato e non è scaduto, posso generare un nuovo access token
        Utente utente = utenteRepository.findById(refreshTokenEntity.getUserId())
                .orElseThrow(() -> new NonAutorizzatoException("Sessione scaduta, effettua nuovamente il login"));

        return jwtService.generateAccessToken(utente);
    }

    public void logout(String refreshJwt) {
        refreshTokenRepository.findByToken(refreshJwt)
                .ifPresent(rt -> {
                    rt.setRevocato(true);
                    refreshTokenRepository.save(rt);
                });
    }

    public void registraNuovoUtente(RegistrazioneRequest request) {

        // verifico prima che il piano scelto esista davvero nel catalogo
        if (!abbonamentoRepository.existsById(request.tipoAbbonamentoId())) {
            throw new RichiestaNonValidaException("Piano abbonamento non valido");
        }

        Optional<Utente> utenteEsistente = utenteRepository.findByEmail(request.email());

        if (utenteEsistente.isPresent() && utenteEsistente.get().isAttivo()) {
            throw new RisorsaGiaEsistenteException("Email già registrata");
        }

        if (utenteEsistente.isPresent()) {

            // l'email esiste ma l'account è disattivato: riattivo invece di creare un nuovo utente
            Utente utente = utenteEsistente.get();
            utente.setNome(request.nome());
            utente.setCognome(request.cognome());
            utente.setHashedPassword(passwordEncoder.encode(request.password()));
            utente.setAttivo(true);
            utenteRepository.save(utente);

            // aggiorno (o creo, se per qualche motivo mancasse) l'abbonamento collegato
            Optional<AbbonamentoUtente> abbonamentoEsistente = abbonamentoUtenteRepository.findByUserId(utente.getId());

            if (abbonamentoEsistente.isPresent()) {
                AbbonamentoUtente abbonamentoUtente = abbonamentoEsistente.get();
                abbonamentoUtente.setTipoAbbonamentoId(request.tipoAbbonamentoId());
                abbonamentoUtenteRepository.save(abbonamentoUtente);
            } else {
                AbbonamentoUtente nuovoAbbonamentoUtente = new AbbonamentoUtente(
                        null,
                        utente.getId(),
                        request.tipoAbbonamentoId(),
                        LocalDateTime.now()
                );
                abbonamentoUtenteRepository.save(nuovoAbbonamentoUtente);
            }

        } else {

            // nuova registrazione
            Utente nuovoUtente = new Utente(
                    null,
                    request.nome(),
                    request.cognome(),
                    request.email(),
                    passwordEncoder.encode(request.password()),
                    true,
                    LocalDateTime.now());

            nuovoUtente = utenteRepository.save(nuovoUtente);

            AbbonamentoUtente abbonamentoUtente = new AbbonamentoUtente(
                    null,
                    nuovoUtente.getId(),
                    request.tipoAbbonamentoId(),
                    LocalDateTime.now()
            );
            abbonamentoUtenteRepository.save(abbonamentoUtente);
        }
    }


}