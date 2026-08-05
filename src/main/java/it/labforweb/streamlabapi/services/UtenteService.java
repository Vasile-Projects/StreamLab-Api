package it.labforweb.streamlabapi.services;

import it.labforweb.streamlabapi.dtos.CambioPasswordRequest;
import it.labforweb.streamlabapi.dtos.UtenteResponse;
import it.labforweb.streamlabapi.exceptions.RichiestaNonValidaException;
import it.labforweb.streamlabapi.exceptions.NonAutorizzatoException;
import it.labforweb.streamlabapi.exceptions.RisorsaNonTrovataException;
import it.labforweb.streamlabapi.models.AbbonamentoUtente;
import it.labforweb.streamlabapi.models.RefreshToken;
import it.labforweb.streamlabapi.models.Utente;
import it.labforweb.streamlabapi.repositories.AbbonamentoRepository;
import it.labforweb.streamlabapi.repositories.AbbonamentoUtenteRepository;
import it.labforweb.streamlabapi.repositories.RefreshTokenRepository;
import it.labforweb.streamlabapi.repositories.UtenteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtenteService {
    private final UtenteRepository utenteRepository;
    private final AbbonamentoUtenteRepository abbonamentoUtenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AbbonamentoRepository abbonamentoRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UtenteService(UtenteRepository utenteRepository, AbbonamentoUtenteRepository abbonamentoUtenteRepository,
                         PasswordEncoder passwordEncoder, AbbonamentoRepository abbonamentoRepository,
                         RefreshTokenRepository refreshTokenRepository) {
        this.utenteRepository = utenteRepository;
        this.abbonamentoUtenteRepository = abbonamentoUtenteRepository;
        this.passwordEncoder = passwordEncoder;
        this.abbonamentoRepository = abbonamentoRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public UtenteResponse me(Integer userId) {

        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(()-> new RisorsaNonTrovataException("Utente non trovato"));

        AbbonamentoUtente abbonamentoUtente = abbonamentoUtenteRepository.findByUserId(userId)
                .orElseThrow(()-> new RisorsaNonTrovataException("Abbonamento non trovato"));
        return new UtenteResponse(utente, abbonamentoUtente);
    }

    public void aggiornaPassword(Integer userId, CambioPasswordRequest request) {
        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato"));

        if (!passwordEncoder.matches(request.vecchiaPassword(), utente.getHashedPassword())) {
            throw new NonAutorizzatoException("Password errata, riprova");
        }

        utente.setHashedPassword(passwordEncoder.encode(request.nuovaPassword()));
        utenteRepository.save(utente);
    }

    public UtenteResponse cambiaAbbonamento(Integer userId, Integer tipoAbbonamentoId) {

        if (!abbonamentoRepository.existsById(tipoAbbonamentoId)) {
            throw new RichiestaNonValidaException("Piano abbonamento non valido");
        }

        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato"));

        AbbonamentoUtente abbonamentoUtente = abbonamentoUtenteRepository.findByUserId(userId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Abbonamento non trovato"));

        abbonamentoUtente.setTipoAbbonamentoId(tipoAbbonamentoId);
        abbonamentoUtenteRepository.save(abbonamentoUtente);

        return me(userId);
    }

    public void disattivaAccount(Integer userId) {

        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato"));

        utente.setAttivo(false);
        utenteRepository.save(utente);

        List<RefreshToken> tokenAttivi = refreshTokenRepository.findByUserIdAndRevocatoFalse(userId);
        for (RefreshToken token : tokenAttivi) {
            token.setRevocato(true);
        }
        refreshTokenRepository.saveAll(tokenAttivi);
    }
}
